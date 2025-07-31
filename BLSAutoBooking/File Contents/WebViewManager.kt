package com.bls.autobooking.webview

import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.bls.autobooking.BuildConfig

class WebViewManager(private val lifecycleOwner: LifecycleOwner) {
    private val TAG = "WebViewManager"
    private var webView: WebView? = null
    
    fun initializeWebView(webView: WebView) {
        this.webView = webView
        
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            userAgentString = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36"
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            displayZoomControls = false
            setSupportZoom(false)
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            
            // Enable debugging in debug builds
            if (BuildConfig.DEBUG) {
                setWebContentsDebuggingEnabled(true)
            }
        }
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                Log.d(TAG, "Page finished loading: $url")
            }
            
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                val errorMessage = if (error != null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        error.description.toString()
                    } else {
                        "WebView error occurred"
                    }
                } else {
                    "Unknown WebView error"
                }
                
                Log.e(TAG, "WebView error: $errorMessage for URL: ${request?.url}")
            }
            
            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                Log.e(TAG, "HTTP error: ${errorResponse?.statusCode} for URL: ${request?.url}")
            }
        }
        
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                if (consoleMessage != null) {
                    Log.d(TAG, "WebView console: ${consoleMessage.message()} " +
                            "at ${consoleMessage.sourceId()}:${consoleMessage.lineNumber()}")
                }
                return super.onConsoleMessage(consoleMessage)
            }
        }
        
        // Add JavaScript interface for communication
        webView.addJavascriptInterface(JavaScriptInterface(), "Android")
    }
    
    fun cleanup() {
        webView?.apply {
            stopLoading()
            destroy()
        }
        webView = null
    }
    
    inner class JavaScriptInterface {
        @JavascriptInterface
        fun showToast(message: String) {
            // This would be called from JavaScript
            Log.d(TAG, "JavaScript message: $message")
        }
        
        @JavascriptInterface
        fun onBookingSuccess(bookingId: String) {
            Log.d(TAG, "Booking successful with ID: $bookingId")
        }
        
        @JavascriptInterface
        fun onBookingError(error: String) {
            Log.e(TAG, "Booking error: $error")
        }
    }
}