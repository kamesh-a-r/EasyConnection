/**
 * EasyWebViewClient.kt
 * Custom WebViewClient for handling loading and errors
 */
package com.kamesh.easyconnectionsdk.presentation.webview

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * Custom WebViewClient with callbacks for page loading events
 */
class EasyWebViewClient(
    private val onPageStarted: ((url: String) -> Unit)? = null,
    private val onPageFinished: ((url: String) -> Unit)? = null,
    private val onError: ((url: String?, errorCode: Int, description: String) -> Unit)? = null
) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        url?.let { onPageStarted?.invoke(it) }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        url?.let { onPageFinished?.invoke(it) }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        onError?.invoke(
            request?.url?.toString(),
            error?.errorCode ?: -1,
            error?.description?.toString() ?: "Unknown error"
        )
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        // Allow all URLs to load in the WebView
        return false
    }
}
