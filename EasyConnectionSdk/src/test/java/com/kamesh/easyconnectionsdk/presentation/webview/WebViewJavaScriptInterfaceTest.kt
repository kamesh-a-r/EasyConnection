package com.kamesh.easyconnectionsdk.presentation.webview

import com.google.gson.Gson
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for WebViewJavaScriptInterface
 */
class WebViewJavaScriptInterfaceTest {

    private lateinit var jsInterface: WebViewJavaScriptInterface
    private val baseUrl = "https://example.com"
    private val parameters = mapOf(
        "userId" to "12345",
        "token" to "abc123",
        "count" to 42
    )
    private val customHeaders = mapOf(
        "Authorization" to "Bearer token123",
        "X-Custom-Header" to "CustomValue"
    )
    private var receivedMessage: String? = null
    private var receivedData: String? = null

    @Before
    fun setup() {
        receivedMessage = null
        receivedData = null
        
        jsInterface = WebViewJavaScriptInterface(
            baseUrl = baseUrl,
            parameters = parameters,
            customHeaders = customHeaders,
            onMessageReceived = { message, data ->
                receivedMessage = message
                receivedData = data
            }
        )
    }

    @Test
    fun `getBaseUrl returns correct base URL`() {
        // When
        val result = jsInterface.getBaseUrl()

        // Then
        assertEquals(baseUrl, result)
    }

    @Test
    fun `getParameters returns all parameters as JSON`() {
        // When
        val result = jsInterface.getParameters()
        val gson = Gson()
        val parsedParams = gson.fromJson(result, Map::class.java)

        // Then
        assertNotNull(parsedParams)
        assertEquals("12345", parsedParams["userId"])
        assertEquals("abc123", parsedParams["token"])
        assertEquals(42.0, parsedParams["count"])
    }

    @Test
    fun `getParameter returns correct parameter value by key`() {
        // When
        val userId = jsInterface.getParameter("userId")
        val token = jsInterface.getParameter("token")
        val count = jsInterface.getParameter("count")

        // Then
        assertEquals("12345", userId)
        assertEquals("abc123", token)
        assertEquals("42", count)
    }

    @Test
    fun `getParameter returns null for non-existent key`() {
        // When
        val result = jsInterface.getParameter("nonExistentKey")

        // Then
        assertNull(result)
    }

    @Test
    fun `getHeaders returns all headers as JSON`() {
        // When
        val result = jsInterface.getHeaders()
        val gson = Gson()
        val parsedHeaders = gson.fromJson(result, Map::class.java)

        // Then
        assertNotNull(parsedHeaders)
        assertEquals("Bearer token123", parsedHeaders["Authorization"])
        assertEquals("CustomValue", parsedHeaders["X-Custom-Header"])
    }

    @Test
    fun `getHeader returns correct header value by key`() {
        // When
        val auth = jsInterface.getHeader("Authorization")
        val custom = jsInterface.getHeader("X-Custom-Header")

        // Then
        assertEquals("Bearer token123", auth)
        assertEquals("CustomValue", custom)
    }

    @Test
    fun `getHeader returns null for non-existent key`() {
        // When
        val result = jsInterface.getHeader("NonExistent")

        // Then
        assertNull(result)
    }

    @Test
    fun `sendMessage triggers callback with correct message and data`() {
        // Given
        val message = "USER_ACTION"
        val data = """{"action": "click", "element": "button"}"""

        // When
        jsInterface.sendMessage(message, data)

        // Then
        assertEquals(message, receivedMessage)
        assertEquals(data, receivedData)
    }

    @Test
    fun `sendMessage with null data works correctly`() {
        // Given
        val message = "SIMPLE_EVENT"

        // When
        jsInterface.sendMessage(message, null)

        // Then
        assertEquals(message, receivedMessage)
        assertNull(receivedData)
    }

    @Test
    fun `getConfig returns complete configuration as JSON`() {
        // When
        val result = jsInterface.getConfig()
        val gson = Gson()
        val config = gson.fromJson(result, Map::class.java)

        // Then
        assertNotNull(config)
        assertEquals(baseUrl, config["baseUrl"])
        assertTrue(config.containsKey("parameters"))
        assertTrue(config.containsKey("headers"))
    }

    @Test
    fun `interface works with empty parameters and headers`() {
        // Given
        val emptyInterface = WebViewJavaScriptInterface(
            baseUrl = baseUrl,
            parameters = emptyMap(),
            customHeaders = emptyMap()
        )

        // When
        val params = emptyInterface.getParameters()
        val headers = emptyInterface.getHeaders()

        // Then
        assertEquals("{}", params)
        assertEquals("{}", headers)
    }

    @Test
    fun `interface works without callback`() {
        // Given
        val interfaceWithoutCallback = WebViewJavaScriptInterface(
            baseUrl = baseUrl,
            parameters = parameters,
            customHeaders = customHeaders,
            onMessageReceived = null
        )

        // When/Then - Should not throw exception
        interfaceWithoutCallback.sendMessage("TEST", "data")
    }
}
