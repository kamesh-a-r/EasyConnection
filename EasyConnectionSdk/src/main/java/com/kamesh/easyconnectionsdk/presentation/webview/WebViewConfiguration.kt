/**
 * WebViewConfiguration.kt
 * Configuration for EasyWebView
 */
package com.kamesh.easyconnectionsdk.presentation.webview

/**
 * Configuration class for WebView settings
 */
data class WebViewConfiguration(
    val baseUrl: String,
    val enableJavaScript: Boolean = true,
    val enableDomStorage: Boolean = true,
    val enableZoom: Boolean = false,
    val enableCache: Boolean = true,
    val allowFileAccess: Boolean = true,
    val allowContentAccess: Boolean = true,
    val allowUniversalAccessFromFiles: Boolean = true,
    val mixedContentMode: Int = 0, // 0 = MIXED_CONTENT_ALWAYS_ALLOW
    val userAgentString: String? = null,
    val customHeaders: Map<String, String> = emptyMap(),
    val parameters: Map<String, Any> = emptyMap(),
    val javaScriptInterfaceName: String = "EasyConnection",
    val enableDebugging: Boolean = false
) {
    /**
     * Builder class for WebViewConfiguration
     */
    class Builder(private val baseUrl: String) {
        private var enableJavaScript: Boolean = true
        private var enableDomStorage: Boolean = true
        private var enableZoom: Boolean = false
        private var enableCache: Boolean = true
        private var allowFileAccess: Boolean = true
        private var allowContentAccess: Boolean = true
        private var allowUniversalAccessFromFiles: Boolean = true
        private var mixedContentMode: Int = 0
        private var userAgentString: String? = null
        private var customHeaders: Map<String, String> = emptyMap()
        private var parameters: Map<String, Any> = emptyMap()
        private var javaScriptInterfaceName: String = "EasyConnection"
        private var enableDebugging: Boolean = false

        fun withJavaScript(enabled: Boolean) = apply {
            this.enableJavaScript = enabled
        }

        fun withDomStorage(enabled: Boolean) = apply {
            this.enableDomStorage = enabled
        }

        fun withZoom(enabled: Boolean) = apply {
            this.enableZoom = enabled
        }

        fun withCache(enabled: Boolean) = apply {
            this.enableCache = enabled
        }

        fun withFileAccess(enabled: Boolean) = apply {
            this.allowFileAccess = enabled
        }

        fun withContentAccess(enabled: Boolean) = apply {
            this.allowContentAccess = enabled
        }

        fun withUniversalAccess(enabled: Boolean) = apply {
            this.allowUniversalAccessFromFiles = enabled
        }

        fun withMixedContentMode(mode: Int) = apply {
            this.mixedContentMode = mode
        }

        fun withUserAgent(userAgent: String?) = apply {
            this.userAgentString = userAgent
        }

        fun withHeaders(headers: Map<String, String>) = apply {
            this.customHeaders = headers
        }

        fun withParameters(params: Map<String, Any>) = apply {
            this.parameters = params
        }

        fun withJavaScriptInterface(name: String) = apply {
            this.javaScriptInterfaceName = name
        }

        fun withDebugging(enabled: Boolean) = apply {
            this.enableDebugging = enabled
        }

        fun build() = WebViewConfiguration(
            baseUrl = baseUrl,
            enableJavaScript = enableJavaScript,
            enableDomStorage = enableDomStorage,
            enableZoom = enableZoom,
            enableCache = enableCache,
            allowFileAccess = allowFileAccess,
            allowContentAccess = allowContentAccess,
            allowUniversalAccessFromFiles = allowUniversalAccessFromFiles,
            mixedContentMode = mixedContentMode,
            userAgentString = userAgentString,
            customHeaders = customHeaders,
            parameters = parameters,
            javaScriptInterfaceName = javaScriptInterfaceName,
            enableDebugging = enableDebugging
        )
    }
}
