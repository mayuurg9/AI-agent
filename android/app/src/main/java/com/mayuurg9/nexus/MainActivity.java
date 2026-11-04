package com.mayuurg9.nexus;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.webkit.*;
import android.widget.*;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;

public class MainActivity extends Activity {
 private WebView webView;
 private android.content.SharedPreferences prefs;
 private WebViewAssetLoader assetLoader;
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  prefs=getSharedPreferences("nexus",MODE_PRIVATE);
  assetLoader=new WebViewAssetLoader.Builder().addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(this)).build();
  webView=new WebView(this); webView.setBackgroundColor(0xFF080D14);
  WebSettings s=webView.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setAllowFileAccess(false); s.setAllowContentAccess(false);
  webView.addJavascriptInterface(new Bridge(),"NexusBridge");
  webView.setWebViewClient(new WebViewClientCompat(){
   @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){return assetLoader.shouldInterceptRequest(r.getUrl());}
  });
  setContentView(webView); webView.loadUrl("https://appassets.androidplatform.net/assets/index.html");
 }
 private class Bridge {
  @JavascriptInterface public String getBaseUrl(){return prefs.getString("backend_url","");}
  @JavascriptInterface public void setBaseUrl(String url){prefs.edit().putString("backend_url",url==null?"":url.trim().replaceAll("/+$","")).apply();}
  @JavascriptInterface public String getAccessToken(){return prefs.getString("app_token","");}
  @JavascriptInterface public void setAccessToken(String token){prefs.edit().putString("app_token",token==null?"":token).apply();}
  @JavascriptInterface public void openBackendSettings(){runOnUiThread(()->showBackendDialog());}
 }
 private void showBackendDialog(){
  LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
  int pad=(int)(20*getResources().getDisplayMetrics().density); box.setPadding(pad,pad,pad,0);
  TextView help=new TextView(this); help.setText("Enter your deployed NEXUS server HTTPS URL. Do not enter an API key here.");
  EditText input=new EditText(this); input.setSingleLine(true); input.setHint("https://your-nexus-server.example.com"); input.setText(prefs.getString("backend_url",""));
  box.addView(help); box.addView(input);
  new AlertDialog.Builder(this).setTitle("Connect NEXUS backend").setView(box).setNegativeButton("Cancel",null)
   .setNeutralButton("Clear",(d,w)->{prefs.edit().remove("backend_url").apply();webView.evaluateJavascript("window.dispatchEvent(new Event('nexus-config-change'))",null);})
   .setPositiveButton("Save",(d,w)->{
    String url=input.getText().toString().trim().replaceAll("/+$","");
    if(!url.isEmpty()&&!url.matches("^https://[^\\s]+$")){new AlertDialog.Builder(this).setMessage("Use a valid HTTPS URL.").setPositiveButton("OK",null).show();return;}
    prefs.edit().putString("backend_url",url).apply(); webView.evaluateJavascript("window.dispatchEvent(new Event('nexus-config-change'))",null);
   }).show();
 }
 @Override public void onBackPressed(){if(webView!=null&&webView.canGoBack())webView.goBack();else super.onBackPressed();}
}
