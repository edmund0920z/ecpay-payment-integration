package com.payment.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

public class EcpayUtils {
	
	public static String generateCheckMacValue(Map<String, String>params, String hashKey, String hashIV) {
		//使用 TreeMap 自動依 Key 的 Alphabet 順序排序
		Map<String, String> sortedMap =new TreeMap<>(params);
		
		//組裝字串: HashKey=xxx&Key1=Value1&Key2=Value2...&HashIV=yyy
		StringBuilder sb =new StringBuilder();
		sb.append("HashKey=").append(hashKey);
		
		for (Map.Entry<String, String>entry:sortedMap.entrySet()) {
			//記得要排除 CheckMacValue 欄位本身
			if("CheckMacValue".equalsIgnoreCase(entry.getKey()) || entry.getValue() == null) {
				continue;
			}
			sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
		}
		sb.append("&HashIV=").append(hashIV);
		
		//取得完整的原始字串
		String rawString = sb.toString();
		
		//URL Encode 並轉換為小寫 (需符合綠界通換特定字元規則)
		String urlEncoded=urlEncode(rawString).toLowerCase();
		
		//綠界要求的特定字元替換
		urlEncoded = urlEncoded.replace("%2d", "-")
				               .replace("%5f", "_")
				               .replace("%2e", ".")
				               .replace("%21", "!")
				               .replace("%2a", "*")
				               .replace("%28", "(")
				               .replace("%29", ")")
				               .replace("%20", "+")
				               .replace("%7e", "~");
		
		//印出最終要拿去 SHA-256 加密的字串
		System.out.println("SHA-256前的完整字串: "+urlEncoded);
		
		//SHA-256加密並轉大寫
		return sha256(urlEncoded).toUpperCase();
				
	}
	
	private static String urlEncode(String value) {
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		}catch (Exception e){
			throw new RuntimeException("URL Encode Error", e);
		}
	}
	
	private static String sha256(String base) {
		try {
			MessageDigest digest =MessageDigest.getInstance("SHA-256");
			byte[] hash =digest.digest(base.getBytes(StandardCharsets.UTF_8));
			StringBuilder hexString =new StringBuilder();
			for(byte b:hash) {
				String hex=Integer.toHexString(0xff & b);
				if(hex.length()==1)hexString.append("0");
				hexString.append(hex);
			}
			return hexString.toString();
		}catch(Exception ex) {
			throw new RuntimeException("SHA-256 Encryption Error", ex);
		}
	}
	
}
