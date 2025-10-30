/**
 * EasyWebView.kt
 * Main WebView component for the SDK
 */
package com.kamesh.easyconnectionsdk.presentation.webview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView

/**
 * Custom WebView component that integrates with EasyConnection SDK
 * Provides easy setup for local HTML loading and JavaScript communication
 */
class EasyWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    private var configuration: WebViewConfiguration? = null
    private var jsInterface: WebViewJavaScriptInterface? = null

    companion object {
        private const val ASSET_PREFIX = "file:///android_asset/"
    }

    init {
        // Enable WebView debugging in debug builds
        setWebContentsDebuggingEnabled(false)
    }

    /**
     * Configure the WebView with the provided configuration
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun configure(
        config: WebViewConfiguration,
        onMessageReceived: ((message: String, data: String?) -> Unit)? = null
    ) {
        this.configuration = config

        // Apply WebView settings
        settings.apply {
            javaScriptEnabled = config.enableJavaScript
            domStorageEnabled = config.enableDomStorage
            builtInZoomControls = config.enableZoom
            displayZoomControls = false
            
            cacheMode = if (config.enableCache) {
                WebSettings.LOAD_DEFAULT
            } else {
                WebSettings.LOAD_NO_CACHE
            }
            
            allowFileAccess = config.allowFileAccess
            allowContentAccess = config.allowContentAccess

            allowFileAccessFromFileURLs = config.allowUniversalAccessFromFiles
            allowUniversalAccessFromFileURLs = config.allowUniversalAccessFromFiles

            mixedContentMode = config.mixedContentMode

            config.userAgentString?.let {
                userAgentString = it
            }

            // Enable debugging if configured
            setWebContentsDebuggingEnabled(config.enableDebugging)
        }

        // Create and add JavaScript interface
        jsInterface = WebViewJavaScriptInterface(
            baseUrl = config.baseUrl,
            parameters = config.parameters,
            customHeaders = config.customHeaders,
            onMessageReceived = onMessageReceived
        )
        
        addJavascriptInterface(jsInterface!!, config.javaScriptInterfaceName)

        // Set default WebChromeClient
        webChromeClient = WebChromeClient()
    }

    /**
     * Set custom WebViewClient with callbacks
     */
    fun setWebViewCallbacks(
        onPageStarted: ((url: String) -> Unit)? = null,
        onPageFinished: ((url: String) -> Unit)? = null,
        onError: ((url: String?, errorCode: Int, description: String) -> Unit)? = null
    ) {
        webViewClient = EasyWebViewClient(onPageStarted, onPageFinished, onError)
    }

    /**
     * Load HTML file from assets folder
     * @param fileName The name of the HTML file in assets folder (e.g., "index.html")
     */
    fun loadAssetFile(fileName: String) {
        val url = "$ASSET_PREFIX$fileName"
        configuration?.let {
            loadUrl(url, it.customHeaders)
        } ?: loadUrl(url)
    }

    /**
     * Load HTML file from a specific path in assets
     * @param path The path to the HTML file (e.g., "web/index.html")
     */
    fun loadAssetPath(path: String) {
        val url = "$ASSET_PREFIX$path"
        configuration?.let {
            loadUrl(url, it.customHeaders)
        } ?: loadUrl(url)
    }

    /**
     * Load HTML content directly as a string
     * @param htmlContent The HTML content as string
     * @param mimeType The MIME type (default: "text/html")
     * @param encoding The encoding (default: "UTF-8")
     */
    fun loadHtmlContent(
        htmlContent: String,
        mimeType: String = "text/html",
        encoding: String = "UTF-8"
    ) {
        val baseUrl = configuration?.baseUrl ?: "about:blank"
        loadDataWithBaseURL(baseUrl, htmlContent, mimeType, encoding, null)
    }

    /**
     * Load URL from the web with custom headers
     * @param url The URL to load
     */
    fun loadWebUrl(url: String) {
        configuration?.let {
            loadUrl(url, it.customHeaders)
        } ?: loadUrl(url)
    }

    /**
     * Execute JavaScript code in the WebView
     * @param script The JavaScript code to execute
     * @param callback Optional callback to receive result (available from API 19+)
     */
    fun executeJavaScript(script: String, callback: ((String) -> Unit)? = null) {
        evaluateJavascript(script) { result ->
            callback?.invoke(result)
        }
    }

    /**
     * Send data to JavaScript
     * @param functionName The JavaScript function name to call
     * @param data The data to pass (will be converted to JSON)
     */
    fun sendToJavaScript(functionName: String, data: Any) {
        val jsonData = com.google.gson.Gson().toJson(data)
        executeJavaScript("$functionName($jsonData)")
    }

    /**
     * Update parameters dynamically
     * @param newParameters New parameters to merge with existing ones
     */
    fun updateParameters(newParameters: Map<String, Any>) {
        configuration?.let { config ->
            val updatedParams = config.parameters.toMutableMap()
            updatedParams.putAll(newParameters)
            
            val updatedConfig = config.copy(parameters = updatedParams)
            this.configuration = updatedConfig
            
            // Recreate JavaScript interface with updated parameters
            jsInterface = WebViewJavaScriptInterface(
                baseUrl = updatedConfig.baseUrl,
                parameters = updatedParams,
                customHeaders = updatedConfig.customHeaders,
                onMessageReceived = jsInterface?.let { js -> 
                    // Preserve the callback
                    { msg: String, data: String? -> js.sendMessage(msg, data) }
                }
            )
            
            // Update the interface (note: this requires reload to take effect)
            removeJavascriptInterface(updatedConfig.javaScriptInterfaceName)
            addJavascriptInterface(jsInterface!!, updatedConfig.javaScriptInterfaceName)
        }
    }

    /**
     * Get current configuration
     */
    fun getConfiguration(): WebViewConfiguration? = configuration

    /**
     * Clear cache and cookies
     */
    fun clearCache() {
        clearCache(true)
        android.webkit.CookieManager.getInstance().removeAllCookies(null)
    }
}
