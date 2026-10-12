package com.suishen.ledger;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.app.Notification;import android.os.Bundle;import android.content.SharedPreferences;import org.json.*;
import java.text.SimpleDateFormat;import java.util.*;import java.util.regex.*;
public class PaymentNotificationService extends NotificationListenerService {
 public void onNotificationPosted(StatusBarNotification sbn) {
 String pkg=sbn.getPackageName();String source;
 if(pkg.equals("com.tencent.mm"))source="wechat";else if(pkg.equals("com.eg.android.AlipayGphone"))source="alipay";else return;
 Bundle b=sbn.getNotification().extras;
 String title=String.valueOf(b.getCharSequence(Notification.EXTRA_TITLE,""));
 String body=String.valueOf(b.getCharSequence(Notification.EXTRA_TEXT,""));
 String content=title+" "+body;
 if(!Pattern.compile("支付成功|付款成功|成功支付|已支付|支出|付款\\s*成功").matcher(content).find())return;
 if(Pattern.compile("退款|收款|到账|转账收入|验证码|待支付|支付失败").matcher(content).find())return;
 Matcher m=Pattern.compile("(?:¥|￥|人民币|金额[:：]?|支付|付款|支出)\\s*([0-9]+(?:\\.[0-9]{1,2})?)\\s*元?").matcher(content);
 if(!m.find())return;
 try{double amount=Double.parseDouble(m.group(1));if(amount<=0||amount>1000000)return;
 String id=source+":"+sbn.getKey()+":"+sbn.getPostTime()+":"+amount;
 SharedPreferences p=getSharedPreferences("notices",MODE_PRIVATE);
 JSONArray arr=new JSONArray(p.getString("pending","[]"));
 for(int i=0;i<arr.length();i++)if(id.equals(arr.getJSONObject(i).optString("id")))return;
 JSONObject obj=new JSONObject();obj.put("id",id);obj.put("source",source);obj.put("amount",amount);
 obj.put("note",content.substring(0,Math.min(100,content.length())));
 obj.put("date",new SimpleDateFormat("yyyy-MM-dd'T'HH:mm",Locale.US).format(new Date(sbn.getPostTime())));
 arr.put(obj);p.edit().putString("pending",arr.toString()).apply();
 }catch(Exception ignored){}
 }
}