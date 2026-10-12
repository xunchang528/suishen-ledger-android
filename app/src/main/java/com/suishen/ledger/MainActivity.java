package com.suishen.ledger;
import android.app.*;import android.os.*;import android.content.*;import android.webkit.*;import android.net.Uri;import org.json.*;
public class MainActivity extends Activity {
 WebView web; ValueCallback<Uri[]> upload; static final int PICK=7,SAVE=8; String backupData;
 @Override public void onCreate(Bundle state){super.onCreate(state);web=new WebView(this);setContentView(web);
 web.getSettings().setJavaScriptEnabled(true);web.getSettings().setDomStorageEnabled(true);web.getSettings().setAllowFileAccess(true);
 web.addJavascriptInterface(new Bridge(),"AndroidLedger");
 web.setWebChromeClient(new WebChromeClient(){@Override public boolean onShowFileChooser(WebView w,ValueCallback<Uri[]> cb,FileChooserParams p){upload=cb;try{startActivityForResult(p.createIntent(),PICK);return true;}catch(Exception e){upload=null;return false;}}});
 web.setWebViewClient(new WebViewClient());web.loadUrl("file:///android_asset/index.html");
 }
 @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==PICK&&upload!=null){upload.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(c,d));upload=null;}if(r==SAVE&&c==RESULT_OK&&d!=null&&backupData!=null){try(java.io.OutputStream os=getContentResolver().openOutputStream(d.getData())){os.write(backupData.getBytes(java.nio.charset.StandardCharsets.UTF_8));}catch(Exception ignored){}backupData=null;}}
 @Override protected void onResume(){super.onResume();if(web!=null)web.evaluateJavascript("window.dispatchEvent(new Event('android-notifications'))",null);}
 class Bridge {
 @JavascriptInterface public void openNotificationSettings(){runOnUiThread(()->startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")));}
 @JavascriptInterface public String getPending(){return getSharedPreferences("notices",MODE_PRIVATE).getString("pending","[]");}
 @JavascriptInterface public void ack(String id){SharedPreferences p=getSharedPreferences("notices",MODE_PRIVATE);try{JSONArray a=new JSONArray(p.getString("pending","[]")),out=new JSONArray();for(int j=0;j<a.length();j++){JSONObject o=a.getJSONObject(j);if(!id.equals(o.optString("id")))out.put(o);}p.edit().putString("pending",out.toString()).apply();}catch(Exception ignored){}}
 @JavascriptInterface public void saveBackup(String filename,String data){runOnUiThread(()->{try{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/json");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,filename);backupData=data;startActivityForResult(i,SAVE);}catch(Exception ignored){} });}
 }
}