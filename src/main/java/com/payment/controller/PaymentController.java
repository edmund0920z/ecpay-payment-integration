package com.payment.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.payment.service.EcpayService;

@RestController
@RequestMapping(path="/api/v1/payment")
public class PaymentController {
	
	private final EcpayService ecpayService;
	
	public PaymentController(EcpayService ecpayService) {
		this.ecpayService =ecpayService;
	}
	
	//發起結帳請求 (回傳 HTML 字串直接在瀏覽器呈現並自動跳轉綠界)
	@PostMapping(value="/checkout", produces=MediaType.TEXT_HTML_VALUE)
	public String checkout(@RequestParam String itemName, @RequestParam Integer totalAmount) {
		return ecpayService.createOrderAndGetForm(itemName, totalAmount);
	}
	
	//接收綠界付款結果回調(Webhook/ReturnURL)
	@PostMapping("/callback")
	public String paymentCallback(@RequestParam Map<String, String>params) {
		return ecpayService.processCallback(params);
	}
	
}
