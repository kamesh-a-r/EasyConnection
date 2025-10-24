/**
 * WebViewDemoActivity.kt
 * Demo Activity showing how to use EasyWebView with the SDK
 */
package com.kamesh.easyconnection

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.kamesh.easyconnectionsdk.data.network.EasyConnectionClient
import com.kamesh.easyconnectionsdk.presentation.webview.EasyWebView
import com.kamesh.easyconnectionsdk.presentation.webview.EasyWebViewHelper
import com.kamesh.easyconnectionsdk.presentation.webview.WebViewConfiguration

class WebViewDemoActivity : AppCompatActivity() {

    private lateinit var webView: EasyWebView

    companion object {
        private const val TAG = "WebViewDemoActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview_demo)
        
        // Get WebView from layout
        webView = findViewById(R.id.easyWebView)

        setupWebView()
        setupBackPressHandler()
    }

    private fun setupWebView() {
        try {
            // Method 1: Using SDK configuration (recommended)
            // Make sure EasyConnectionClient is initialized first
            if (isSDKInitialized()) {
                setupWithSDKConfig()
            } else {
                // Method 2: Standalone configuration
                setupStandalone()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up WebView", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Setup WebView using EasyConnectionClient configuration
     */
    private fun setupWithSDKConfig() {
        // Custom parameters to pass to JavaScript
        val params = mapOf(
            "userId" to "12345",
            "userName" to "John Doe",
            "theme" to "dark",
            "version" to "1.0.0",
            "apiKey" to "demo_key_123"
        )

        // Setup WebView with SDK configuration
        EasyWebViewHelper.setupWebView(
            webView = webView,
            additionalParams = params,
            onMessageReceived = { message, data ->
                Log.d(TAG, "Message received: $message, Data: $data")
                runOnUiThread {
                    Toast.makeText(this, "JS Message: $message", Toast.LENGTH_SHORT).show()
                }
            },
            onPageStarted = { url ->
                Log.d(TAG, "Page started loading: $url")
            },
            onPageFinished = { url ->
                Log.d(TAG, "Page finished loading: $url")
                
                // You can send data to JavaScript after page loads
                sendDataToWebView()
            },
            onError = { url, errorCode, description ->
                Log.e(TAG, "WebView error: $errorCode - $description at $url")
            }
        )

        // Load the sample HTML file
        EasyWebViewHelper.loadSamplePage(webView)
        
        // Or load custom HTML from assets
        // webView.loadAssetFile("your_custom_file.html")
        
        // Or load from web
        // webView.loadWebUrl("https://your-website.com")
    }

    /**
     * Setup WebView as standalone (without SDK)
     */
    private fun setupStandalone() {
        val params = mapOf(
            "mode" to "standalone",
            "appName" to "EasyConnection Demo"
        )

        val config = EasyWebViewHelper.createStandaloneConfiguration(
            baseUrl = "https://api.example.com/",
            parameters = params,
            headers = mapOf("X-Custom-Header" to "CustomValue"),
            enableDebugging = BuildConfig.DEBUG
        )

        webView.configure(
            config = config,
            onMessageReceived = { message, data ->
                Log.d(TAG, "Standalone message: $message, Data: $data")
            }
        )

        webView.setWebViewCallbacks(
            onPageFinished = { url ->
                Log.d(TAG, "Standalone page loaded: $url")
            }
        )

        // Load HTML
        EasyWebViewHelper.loadSamplePage(webView)
    }

    /**
     * Send data from Android to JavaScript
     */
    private fun sendDataToWebView() {
        val data = mapOf(
            "notification" to "Hello from Android!",
            "timestamp" to System.currentTimeMillis()
        )

        // Call a JavaScript function
        webView.sendToJavaScript("receiveFromAndroid", data)
    }

    /**
     * Check if SDK is initialized
     */
    private fun isSDKInitialized(): Boolean {
        return try {
            EasyConnectionClient.getConfiguration()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Setup back press handler using OnBackPressedDispatcher
     * This handles both back button and back gesture navigation
     */
    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    // Disable this callback and let the system handle back press
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }
}
