package com.blsautobooking.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class WebViewHelper(private val context: Context) {

    private var webView: WebView? = null

    fun initializeWebView(): WebView {
        return WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                userAgentString = "Mozilla/5.0 (Linux; Android 10; SM-G975F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36"
            }
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d(TAG, "Page loaded: $url")
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    Log.e(TAG, "WebView error: ${error?.description}")
                }
            }
            
            webView = this
        }
    }

    suspend fun loadUrl(url: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            Handler(Looper.getMainLooper()).post {
                webView?.let { webView ->
                    webView.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            continuation.resume(true)
                        }

                        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                            super.onReceivedError(view, request, error)
                            continuation.resumeWithException(Exception("Failed to load URL: ${error?.description}"))
                        }
                    }
                    webView.loadUrl(url)
                } ?: continuation.resumeWithException(Exception("WebView not initialized"))
            }
        }
    }

    suspend fun executeJavaScript(script: String): String? {
        return suspendCancellableCoroutine { continuation ->
            Handler(Looper.getMainLooper()).post {
                webView?.evaluateJavascript(script) { result ->
                    continuation.resume(result)
                } ?: continuation.resumeWithException(Exception("WebView not initialized"))
            }
        }
    }

    suspend fun fillInputField(selector: String, value: String): Boolean {
        val script = """
            (function() {
                var element = document.querySelector('$selector');
                if (element) {
                    element.value = '$value';
                    element.dispatchEvent(new Event('input', { bubbles: true }));
                    element.dispatchEvent(new Event('change', { bubbles: true }));
                    return true;
                }
                return false;
            })();
        """.trimIndent()

        val result = executeJavaScript(script)
        return result == "true"
    }

    suspend fun clickElement(selector: String): Boolean {
        val script = """
            (function() {
                var element = document.querySelector('$selector');
                if (element) {
                    element.click();
                    return true;
                }
                return false;
            })();
        """.trimIndent()

        val result = executeJavaScript(script)
        return result == "true"
    }

    suspend fun waitForElement(selector: String, timeoutMs: Long = 10000): Boolean {
        val startTime = System.currentTimeMillis()
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            val script = """
                (function() {
                    var element = document.querySelector('$selector');
                    return element !== null;
                })();
            """.trimIndent()

            val result = executeJavaScript(script)
            if (result == "true") {
                return true
            }
            
            kotlinx.coroutines.delay(500)
        }
        
        return false
    }

    suspend fun captureCaptchaImage(): Bitmap? {
        return suspendCancellableCoroutine { continuation ->
            Handler(Looper.getMainLooper()).post {
                webView?.let { webView ->
                    // This is a simplified approach. In a real scenario, you might need to
                    // capture a specific part of the WebView that contains the CAPTCHA
                    val bitmap = Bitmap.createBitmap(
                        webView.width,
                        webView.height,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = android.graphics.Canvas(bitmap)
                    webView.draw(canvas)
                    continuation.resume(bitmap)
                } ?: continuation.resume(null)
            }
        }
    }

    fun destroy() {
        webView?.destroy()
        webView = null
    }

    companion object {
        private const val TAG = "WebViewHelper"
    }
}

