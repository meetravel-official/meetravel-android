package com.life.meetravel

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import java.net.URISyntaxException

class MainActivity : ComponentActivity() {
    var webViewImageUpload: ValueCallback<Array<Uri>>? = null

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val resultCode = result.resultCode
        val data = result.data
        if (resultCode == RESULT_OK)  {
                val results = data?.data!!
                webViewImageUpload!!.onReceiveValue(arrayOf(results))
        } else { // 취소한 경우 초기화
            webViewImageUpload?.onReceiveValue(null)
            webViewImageUpload = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetJavaScriptEnabled")
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
        getPermission()
        myWebView.loadUrl("https://meetravel.life/")

        myWebView.settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
            builtInZoomControls=false
            allowFileAccess=true
            domStorageEnabled=true
            mixedContentMode=WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        myWebView.webChromeClient = object : WebChromeClient() {
            @SuppressLint("IntentReset", "QueryPermissionsNeeded")
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                try {
                    webViewImageUpload = filePathCallback!!

                    // 이미지를 선택하는 Intent 설정
                    val contentSelectionIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    contentSelectionIntent.type = "image/*"

                    // Intent 배열은 더 이상 필요하지 않으므로 제거
                    val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "사용할 앱을 선택해주세요.")

                    // ActivityResultLauncher로 선택된 이미지 처리
                    launcher.launch(chooserIntent)
                } catch (_: Exception) {
                }
                return true
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                request!!.grant(request.resources)
            }
        }

        myWebView.webViewClient = object: WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView,request: WebResourceRequest): Boolean {
                Log.d("tag", request.url.toString())

                if (request.url.scheme == "intent") {
                    try {
                        // Intent 생성
                        val intent = Intent.parseUri(request.url.toString(), Intent.URI_INTENT_SCHEME)

                        // 실행 가능한 앱이 있으면 앱 실행
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                            Log.d("tag", "ACTIVITY: ${intent.`package`}")
                            return true
                        }

                        // Fallback URL이 있으면 현재 웹뷰에 로딩
                        val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                        if (fallbackUrl != null) {
                            view.loadUrl(fallbackUrl)
                            Log.d("tag", "FALLBACK: $fallbackUrl")
                            return true
                        }

                        Log.e("tag", "Could not parse anythings")

                    } catch (e: URISyntaxException) {
                        Log.e("tag", "Invalid intent request", e)
                    }
                }

                // 나머지 서비스 로직 구현

                return false
            }

        }
    }

    // 앨범에 접근할 수 있는 권한
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getPermission() {
        val locationPermission = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val permission = mutableMapOf<String, String>()

        permission["READSTORAGE"] = android.Manifest.permission.READ_EXTERNAL_STORAGE
        permission["WRITESTORAGE"] = android.Manifest.permission.WRITE_EXTERNAL_STORAGE

        val denied = permission.count {
            ContextCompat.checkSelfPermission(
                this,
                it.value
            ) == PackageManager.PERMISSION_DENIED
        }

        if (denied > 0) {
            requestPermissions(permission.values.toTypedArray(), 100)
        } else {
            ActivityCompat.requestPermissions(this, locationPermission, 400)
        }
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

