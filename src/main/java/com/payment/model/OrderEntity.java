package com.payment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="orders")
public class OrderEntity {
	
	@Id
	@Column(name="merchant_trade_no", length=20)
	private String merchantTradeNo; //廠商交易編號 (綠界要求唯一)
	
	@Column(name="item_name", nullable=false, length=200)
	private String itemName; //商品名稱
	
	@Column(name="total_amount", nullable=false, precision=10, scale=2)
	private BigDecimal totalAmount; //總金額
	
	@Column(name="trade_status", nullable=false, length=20)
	private String tradeStatus; //交易狀態(UNPAID, PAID, FAILED)
	
	@Column(name="ecpay_trade_no")
	private String ecpayTradeNo; //綠界交易序號(回調時取得)
	
	@CreationTimestamp
	@Column(name="create_time", updatable=false)
	private LocalDateTime createTime;
	
	@Column(name="payment_date")
	private LocalDateTime paymentDate;
	
	
	//----getter and setter----
	public String getMerchantTradeNo() {
		return merchantTradeNo;
	}

	public void setMerchantTradeNo(String merchantTradeNo) {
		this.merchantTradeNo = merchantTradeNo;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getTradeStatus() {
		return tradeStatus;
	}

	public void setTradeStatus(String tradeStatus) {
		this.tradeStatus = tradeStatus;
	}

	public String getEcpayTradeNo() {
		return ecpayTradeNo;
	}

	public void setEcpayTradeNo(String ecpayTradeNo) {
		this.ecpayTradeNo = ecpayTradeNo;
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}

	public LocalDateTime getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(LocalDateTime paymentDate) {
		this.paymentDate = paymentDate;
	}
	
}
