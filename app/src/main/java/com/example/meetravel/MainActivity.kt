package com.example.meetravel

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myWebView: WebView = findViewById(R.id.webview)
        myWebView.settings.run {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically= true
            setSupportMultipleWindows(true)
        }
        myWebView.webViewClient = WebViewClient()
        myWebView.webChromeClient = WebChromeClient()
        myWebView.loadUrl("https://meetravel.life")
    }

    override fun onBackPressed() {
        val myWebView: WebView =  findViewById(R.id.webview)
        if (myWebView.canGoBack()) {
            myWebView.goBack()
        }
        else {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}

