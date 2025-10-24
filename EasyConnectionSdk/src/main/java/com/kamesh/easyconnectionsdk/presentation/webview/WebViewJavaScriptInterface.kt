/**
 * WebViewJavaScriptInterface.kt
 * JavaScript interface for communication between WebView and Android
 */
package com.kamesh.easyconnectionsdk.presentation.webview

import android.webkit.JavascriptInterface
import com.google.gson.Gson
import com.google.gson.JsonObject

/**
 * JavaScript interface for bidirectional communication
 */
class WebViewJavaScriptInterface(
    private val baseUrl: String,
    private val parameters: Map<String, Any>,
    private val customHeaders: Map<String, String> = emptyMap(),
    private val onMessageReceived: ((message: String, data: String?) -> Unit)? = null
) {
    private val gson = Gson()

    /**
     * Get the base URL from Android
     * Called from JavaScript: EasyConnection.getBaseUrl()
     */
    @JavascriptInterface
    fun getBaseUrl(): String {
        return baseUrl
    }

    /**
     * Get all parameters as JSON string
     * Called from JavaScript: EasyConnection.getParameters()
     */
    @JavascriptInterface
    fun getParameters(): String {
        return gson.toJson(parameters)
    }

    /**
     * Get a specific parameter by key
     * Called from JavaScript: EasyConnection.getParameter('key')
     */
    @JavascriptInterface
    fun getParameter(key: String): String? {
        return parameters[key]?.toString()
    }

    /**
     * Get custom headers as JSON string
     * Called from JavaScript: EasyConnection.getHeaders()
     */
    @JavascriptInterface
    fun getHeaders(): String {
        return gson.toJson(customHeaders)
    }

    /**
     * Get a specific header value
     * Called from JavaScript: EasyConnection.getHeader('key')
     */
    @JavascriptInterface
    fun getHeader(key: String): String? {
        return customHeaders[key]
    }

    /**
     * Send a message from JavaScript to Android
     * Called from JavaScript: EasyConnection.sendMessage('type', '{data: "value"}')
     */
    @JavascriptInterface
    fun sendMessage(message: String, data: String? = null) {
        onMessageReceived?.invoke(message, data)
    }

    /**
     * Log from JavaScript to Android logcat
     * Called from JavaScript: EasyConnection.log('message')
     */
    @JavascriptInterface
    fun log(message: String) {
        android.util.Log.d("EasyConnection", "JS Log: $message")
    }

    /**
     * Get configuration as JSON
     * Called from JavaScript: EasyConnection.getConfig()
     */
    @JavascriptInterface
    fun getConfig(): String {
        val config = JsonObject().apply {
            addProperty("baseUrl", baseUrl)
            add("parameters", gson.toJsonTree(parameters))
            add("headers", gson.toJsonTree(customHeaders))
        }
        return gson.toJson(config)
    }
}
