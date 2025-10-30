/**
 * EasyWebViewHelper.kt
 * Helper class for simplified WebView integration with EasyConnection SDK
 */
package com.kamesh.easyconnectionsdk.presentation.webview

import android.content.Context
import com.kamesh.easyconnectionsdk.data.network.EasyConnectionClient

/**
 * Helper class to simplify WebView setup with EasyConnection SDK
 */
object EasyWebViewHelper {

    /**
     * Create a WebViewConfiguration using the EasyConnectionClient's configuration
     * This automatically syncs the base URL and headers from the SDK
     */
    fun createConfigurationFromSDK(
        additionalParams: Map<String, Any> = emptyMap(),
        javaScriptInterfaceName: String = "EasyConnection",
        enableDebugging: Boolean = false
    ): WebViewConfiguration {
        val sdkConfig = EasyConnectionClient.getConfiguration()
        
        return WebViewConfiguration.Builder(sdkConfig.baseUrl)
            .withHeaders(sdkConfig.additionalHeaders)
            .withParameters(additionalParams)
            .withJavaScriptInterface(javaScriptInterfaceName)
            .withDebugging(enableDebugging)
            .build()
    }

    /**
     * Quick setup for EasyWebView with SDK configuration
     */
    fun setupWebView(
        webView: EasyWebView,
        additionalParams: Map<String, Any> = emptyMap(),
        onMessageReceived: ((message: String, data: String?) -> Unit)? = null,
        onPageStarted: ((url: String) -> Unit)? = null,
        onPageFinished: ((url: String) -> Unit)? = null,
        onError: ((url: String?, errorCode: Int, description: String) -> Unit)? = null
    ) {
        val config = createConfigurationFromSDK(additionalParams)
        
        webView.configure(config, onMessageReceived)
        webView.setWebViewCallbacks(onPageStarted, onPageFinished, onError)
    }

    /**
     * Create a standalone WebViewConfiguration (not tied to SDK)
     */
    fun createStandaloneConfiguration(
        baseUrl: String,
        parameters: Map<String, Any> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        javaScriptInterfaceName: String = "EasyConnection",
        enableDebugging: Boolean = false
    ): WebViewConfiguration {
        return WebViewConfiguration.Builder(baseUrl)
            .withHeaders(headers)
            .withParameters(parameters)
            .withJavaScriptInterface(javaScriptInterfaceName)
            .withDebugging(enableDebugging)
            .build()
    }

    /**
     * Load the sample HTML file included in the SDK
     */
    fun loadSamplePage(webView: EasyWebView) {
        webView.loadAssetFile("easyconnection_sample.html")
    }

    /**
     * Helper to inject SDK configuration into existing WebView
     */
    fun injectSDKConfiguration(webView: EasyWebView) {
        val sdkConfig = EasyConnectionClient.getConfiguration()
        
        val script = """
            if (typeof EasyConnection !== 'undefined') {
                console.log('EasyConnection SDK integrated');
                console.log('Base URL: ' + EasyConnection.getBaseUrl());
            }
        """.trimIndent()
        
        webView.executeJavaScript(script)
    }
}
