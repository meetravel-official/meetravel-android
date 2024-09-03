package com.life.meetravel

import android.os.Bundle
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myWebView: WebView = findViewById(R.id.webview)
        // FCM 토큰 가져오기
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("메인", "FCM 등록 토큰 가져오기 실패", task.exception)
                return@addOnCompleteListener
            }

            // 새 FCM 등록 토큰 가져오기
            val token = task.result

            // 토큰 사용 (예: 로그에 출력)
            Log.d("메인", "FCM 토큰: $token")

            // 필요한 경우 서버로 토큰 전송
            // sendTokenToServer(token)
        }



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
            super.onBackPressedDispatcher.onBackPressed()
        }
    }
}

