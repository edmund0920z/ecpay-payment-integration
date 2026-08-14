# 綠界金流整合與訂單系統 (ECPay Payment Integration API)

基於 Spring Boot 3 與 MS SQL Server 開發的後端 RESTful API 系統。針對第三方金流串接情境進行設計，整合綠界科技 (ECPay) API 實現安全結帳表單生成、SHA-256 CheckMacValue 數位簽章防偽驗證，並透過冪等性 (Idempotency) 機制防止 Webhook 重複扣款與資料狀態異常。

---

## 專案結構 (Project Structure)

```text
├─ src/                           # Spring Boot 後端程式碼
├─ docs/                          # 存放測試與資料庫佐證圖片
│  ├─ postman-callback-result.png # Postman 模擬 Callback 成功截圖 (1|OK)
│  └─ db-order-paid-result.png    # MS SQL Server 訂單更新為 PAID 之資料庫截圖
├─ pom.xml                        # Maven 依賴管理
└─ README.md                      # 專案說明文件

---

## 技術選型
* **Core Framework: Spring Boot 3.x
* **Database: SQL Server / Spring Data JPA
* **Payment Gateway: 綠界科技 ECPay AioCheckOut V5 API
* **Security & Encryption: SHA-256 / CheckMacValue / Dynamic URL Encoding

---

## 金流 Callback 與狀態驗證 (Payment Callback & Idempotency Result)
### 1. 數位簽章防偽與 Webhook 機制
* **壓碼驗證 (CheckMacValue)：接收綠界 Callback 時，將傳入參數進行 A-Z 字典排序，結合 HashKey/HashIV 以 SHA-256 演算法計算數位簽章，防範第三方偽造或竄改請求。
* **綠界通訊協定：成功處理解析後，回應 1|OK 告知綠界伺服器已順利接收，停止重複重試機制。

### 2. 測試與驗證結果
* **HTTP 200 OK：成功接收金流 Callback，回應 1|OK 響應訊息。
* **冪等性防護 (Idempotency)：重複發送相同訂單的 Callback 時，系統自動識別已處理狀態（PAID），跳過寫入並直接回應 1|OK，防止重複處理。
* **資料庫驗證：交易成功後，trade_status 精準更新為 PAID，並寫入 ecpay_trade_no 與 payment_date。

---

## 專案架構亮點
1. **數位簽章驗證：實作 SHA-256 壓碼演算法，並針對綠界規範對 URL Encoding 特殊字元（如 ~、* 等）進行精準替換，確保防偽印章驗證無誤。
2. **冪等性設計 (Idempotency)：在 Callback 處理流程中加入狀態鎖定與攔截，防止網路延遲重複發送通知所導致的多次狀態更新或重複出貨問題。
3. **敏感資訊控管：採用環境變數 (Environment Variables) 注入 HashKey 與 HashIV 金鑰，符合企業級資安最佳實踐 (Best Practice)。

