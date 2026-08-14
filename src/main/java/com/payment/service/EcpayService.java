package com.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.payment.model.OrderEntity;
import com.payment.repository.OrderRepository;
import com.payment.util.EcpayUtils;

@Service
public class EcpayService {
	
	@Value("${ecpay.payment.merchant_id}")
	private String merchantId;
	
	@Value("${ecpay.payment.hash_key}")
	private String hashKey;
	
	@Value("${ecpay.payment.hash_iv}")
	private String hashIV;
	
	@Value("${ecpay.payment.checkout_url}")
	private String checkoutUrl;
	
	private final OrderRepository orderRepository;
	
	public EcpayService(OrderRepository orderRepository) {
		this.orderRepository=orderRepository;
	}
	
	
	//1.建立本地訂單，並組裝自動提交給綠界的 HTML Form 表單
	public String createOrderAndGetForm(String itemName, Integer totalAmount) {
		
		//如果 itemName 包含逗點 (被重複傳遞)，只取第一個商品名稱
		if(itemName != null && itemName.contains(",")) {
			itemName =itemName.split(",")[0];
		}
		
		//生成唯一廠商交易編號 (長度限製 20 字元以內)
		String merchantTradeNo ="ORD"+System.currentTimeMillis()+(int)(Math.random()*1000);
		
		//儲存至資料庫 (狀態設為 UNPAID)
		OrderEntity order =new OrderEntity();
		order.setMerchantTradeNo(merchantTradeNo);
		order.setItemName(itemName);
		order.setTotalAmount(BigDecimal.valueOf(totalAmount));
		order.setTradeStatus("UNPAID");
		orderRepository.save(order);
		
		//準備綠界要求的必要參數
		DateTimeFormatter formatter =DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		String merchantTradeDate =LocalDateTime.now().format(formatter);
		
		Map<String, String>params =new HashMap<>();
		params.put("MerchantID", merchantId);
		params.put("MerchantTradeNo", merchantTradeNo);
		params.put("MerchantTradeDate", merchantTradeDate);
		params.put("PaymentType", "aio");
		params.put("TotalAmount", String.valueOf(totalAmount));
		params.put("TradeDesc", "Test Order Transaction");
		params.put("ItemName", itemName);
		
		//綠界付款成功後的背景通知 URL (Webhook)
		params.put("ReturnURL", "https://example.com/api/v1/payment/callback");
		params.put("ChoosePayment", "ALL");
		params.put("EncryptType","1" );
		
		//計算 CheckMacValue 並放進參數
		String checkMacValue =EcpayUtils.generateCheckMacValue(params, hashKey, hashIV);
		params.put("CheckMacValue", checkMacValue);
		
		//生成 HTML 表單 (包含 Javascript 自動 submit)
		StringBuilder form =new StringBuilder();
		form.append("<form id='ecpayForm' action='").append(checkoutUrl).append("' method='POST' accept-charset='UTF-8'>");
		for(Map.Entry<String, String>entry:params.entrySet()) {
			form.append("<input type='hidden' name='").append(entry.getKey()).append("' value='")
			.append(entry.getValue()).append("'/>");
			
		}
		form.append("</form>");
		form.append("<script>document.getElementById('ecpayForm').submit();</script>");
		
		System.out.println("====綠界送出表單 HTML====");
		System.out.println(form.toString());
		
		System.out.println("=== 傳給綠界的參數 ===");
		System.out.println("MerchantID: " + merchantId);
		System.out.println("HashKey: " + hashKey);
		System.out.println("HashIV: " + hashIV);
		System.out.println("CheckMacValue: " + checkMacValue);
		
		return form.toString();
	}
	
	//2.處理綠界付款結果回調(Webhook)
	public String processCallback(Map<String, String>callbackParams) {
		
		//驗證 CheckMacValue防偽壓碼
		//呼叫verifyCheckMacValue的方法(用postman測試時，先把106-110註解，測完後在取消註解，用ngrok就不用註解)
		boolean isValid=verifyCheckMacValue(callbackParams);
		
		if(!isValid) {
			return "0|CheckMacValue Verify Fail"; //驗證失敗
		}
	
		//檢查RtnCode(1 代表成功)
		String rtnCode =callbackParams.get("RtnCode");
		String merchantTradeNo =callbackParams.get("MerchantTradeNo");
		String ecpayTradeNo =callbackParams.get("TradeNo");
		
		//搜尋資料庫中的訂單
		OrderEntity order =orderRepository.findById(merchantTradeNo).orElse(null);
		if(order !=null) {
			
			//在更新狀態以為，先檢查:如果訂單早就已經是 PAID，代表之前處理過了，直接回傳 1|OK 結束
			if("PAID".equals(order.getTradeStatus())) {
				return "1|OK";
			}
			
			//處理狀態更新邏輯
			if("1".equals(rtnCode)) {
				order.setTradeStatus("PAID");
				order.setEcpayTradeNo(ecpayTradeNo);
				order.setPaymentDate(LocalDateTime.now());
				orderRepository.save(order);
			}else {
				order.setTradeStatus("FAILED");
				orderRepository.save(order);
			}
		}
		
		//綠界規定: 接受回調成功必須回傳 "1|OK"，綠界才會停止重複發送通知
		return "1|OK";
	}
	
	//-----綠界檢查碼驗證機制 / 交易簽名驗證-----
	public boolean verifyCheckMacValue(Map<String, String>params) {
		
		//1.先取出綠界傳過來的CheckMacValue
		String receivedCheckMac =params.get("CheckMacValue");
		if(receivedCheckMac == null || receivedCheckMac.isEmpty()) {
			return false;
		}
		
		//2.複製一份 params 並移除 CheckMacValue 再進行重新計算
		Map<String, String> checkParams =new HashMap<>(params);
		checkParams.remove("CheckMacValue");
		
		//3.後端重新計算出 CheckMacValue
		String calculatedCheckMacValue =EcpayUtils.generateCheckMacValue(checkParams, hashKey, hashIV);
		
		//印出日誌以進行比對排查
		System.out.println("收到綠界的 CheckMacValue: "+receivedCheckMac);
		System.out.println("後端自己算的 CheckMacValue: "+calculatedCheckMacValue);
		
		
		//4.對比兩者是否相同(不分大小寫)
		return calculatedCheckMacValue.equalsIgnoreCase(receivedCheckMac);
	}
}
