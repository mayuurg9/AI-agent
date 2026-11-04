package com.mayuurg9.nexus

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private val prefs by lazy { getSharedPreferences("nexus_settings", MODE_PRIVATE) }
    private lateinit var assetLoader: WebViewAssetLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(8, 13, 20)
        window.navigationBarColor = android.graphics.Color.rgb(8, 13, 20)
        assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()
        webView = WebView(this).apply {
            setBackgroundColor(android.graphics.Color.rgb(8, 13, 20))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            addJavascriptInterface(NexusBridge(), "NexusBridge")
            webViewClient = object : WebViewClientCompat() {
                override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest) =
                    assetLoader.shouldInterceptRequest(request.url)
            }
        }
        setContentView(webView)
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html")
    }

    inner class NexusBridge {
        @JavascriptInterface fun getBaseUrl(): String = prefs.getString("backend_url", "") ?: ""
        @JavascriptInterface fun setBaseUrl(value: String?) {
            prefs.edit().putString("backend_url", value.orEmpty().trim().trimEnd('/')).apply()
        }
        @JavascriptInterface fun getAccessToken(): String = prefs.getString("app_token", "") ?: ""
        @JavascriptInterface fun setAccessToken(value: String?) {
            prefs.edit().putString("app_token", value.orEmpty().trim()).apply()
        }
        @JavascriptInterface fun openBackendSettings() { runOnUiThread { showBackendSettings() } }
    }

    private fun showBackendSettings() {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val p = (20 * resources.displayMetrics.density).toInt()
            setPadding(p, p, p, 0)
        }
        val help = TextView(this).apply {
            text = "Enter your deployed NEXUS server HTTPS URL. Provider API keys stay on the server."
        }
        val url = EditText(this).apply {
            hint = "https://your-nexus-server.example.com"
            setSingleLine(true)
            setText(prefs.getString("backend_url", ""))
        }
        val token = EditText(this).apply {
            hint = "App access token (if configured)"
            setSingleLine(true)
            setText(prefs.getString("app_token", ""))
        }
        box.addView(help); box.addView(url); box.addView(token)
        AlertDialog.Builder(this).setTitle("NEXUS connection").setView(box)
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Clear") { _, _ ->
                prefs.edit().remove("backend_url").remove("app_token").apply()
            }
            .setPositiveButton("Save") { _, _ ->
                val base = url.text.toString().trim().trimEnd('/')
                if (base.isNotEmpty() && !base.startsWith("https://")) {
                    AlertDialog.Builder(this).setMessage("Use a valid HTTPS backend URL.")
                        .setPositiveButton("OK", null).show()
                    return@setPositiveButton
                }
                prefs.edit().putString("backend_url", base)
                    .putString("app_token", token.text.toString().trim()).apply()
            }.show()
    }

    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }

    override fun onDestroy() {
        if (::webView.isInitialized) webView.destroy()
        super.onDestroy()
    }
}
