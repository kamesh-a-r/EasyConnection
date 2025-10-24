package com.kamesh.easyconnectionsdk.data.network.interceptors

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

/**
 * Unit tests for network interceptors
 */
class InterceptorsTest {

    // Helper to create a mock chain
    private fun createMockChain(
        request: Request,
        response: Response
    ): Interceptor.Chain {
        return object : Interceptor.Chain {
            override fun request(): Request = request
            override fun proceed(request: Request): Response = response
            override fun connection(): Connection? = null
            override fun call(): Call = throw NotImplementedError()
            override fun connectTimeoutMillis(): Int = 0
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun readTimeoutMillis(): Int = 0
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun writeTimeoutMillis(): Int = 0
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
        }
    }

    // Helper to create a basic request
    private fun createRequest(url: String = "https://api.example.com/test"): Request {
        return Request.Builder()
            .url(url)
            .build()
    }

    // Helper to create a successful response
    private fun createSuccessResponse(request: Request, body: String = "success"): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(body.toResponseBody("application/json".toMediaTypeOrNull()))
            .build()
    }

    // ========== AuthInterceptor Tests ==========

    @Test
    fun `AuthInterceptor adds Bearer token to request`() {
        // Given
        val authToken = "test-token-123"
        val interceptor = AuthInterceptor(authToken)
        val request = createRequest()
        val response = createSuccessResponse(request)
        
        var interceptedRequest: Request? = null
        val chain = object : Interceptor.Chain {
            override fun request(): Request = request
            override fun proceed(request: Request): Response {
                interceptedRequest = request
                return response
            }
            override fun connection(): Connection? = null
            override fun call(): Call = throw NotImplementedError()
            override fun connectTimeoutMillis(): Int = 0
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun readTimeoutMillis(): Int = 0
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun writeTimeoutMillis(): Int = 0
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
        }

        // When
        interceptor.intercept(chain)

        // Then
        assertNotNull(interceptedRequest)
        assertEquals("Bearer $authToken", interceptedRequest!!.header("Authorization"))
    }

    @Test
    fun `AuthInterceptor preserves original request method and url`() {
        // Given
        val authToken = "test-token"
        val interceptor = AuthInterceptor(authToken)
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), "{}")
        val request = Request.Builder()
            .url("https://api.example.com/users")
            .post(requestBody)
            .build()
        val response = createSuccessResponse(request)
        
        var interceptedRequest: Request? = null
        val chain = object : Interceptor.Chain {
            override fun request(): Request = request
            override fun proceed(request: Request): Response {
                interceptedRequest = request
                return response
            }
            override fun connection(): Connection? = null
            override fun call(): Call = throw NotImplementedError()
            override fun connectTimeoutMillis(): Int = 0
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun readTimeoutMillis(): Int = 0
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun writeTimeoutMillis(): Int = 0
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
        }

        // When
        interceptor.intercept(chain)

        // Then
        assertNotNull(interceptedRequest)
        assertEquals("POST", interceptedRequest!!.method)
        assertEquals("https://api.example.com/users", interceptedRequest.url.toString())
    }

    // ========== HeaderInterceptor Tests ==========

    @Test
    fun `HeaderInterceptor adds all custom headers`() {
        // Given
        val headers = mapOf(
            "X-Custom-Header" to "CustomValue",
            "X-Api-Version" to "v2",
            "X-Client-Id" to "android-123"
        )
        val interceptor = HeaderInterceptor(headers)
        val request = createRequest()
        val response = createSuccessResponse(request)
        
        var interceptedRequest: Request? = null
        val chain = object : Interceptor.Chain {
            override fun request(): Request = request
            override fun proceed(request: Request): Response {
                interceptedRequest = request
                return response
            }
            override fun connection(): Connection? = null
            override fun call(): Call = throw NotImplementedError()
            override fun connectTimeoutMillis(): Int = 0
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun readTimeoutMillis(): Int = 0
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun writeTimeoutMillis(): Int = 0
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
        }

        // When
        interceptor.intercept(chain)

        // Then
        assertNotNull(interceptedRequest)
        assertEquals("CustomValue", interceptedRequest!!.header("X-Custom-Header"))
        assertEquals("v2", interceptedRequest.header("X-Api-Version"))
        assertEquals("android-123", interceptedRequest.header("X-Client-Id"))
    }

    @Test
    fun `HeaderInterceptor works with empty headers map`() {
        // Given
        val interceptor = HeaderInterceptor(emptyMap())
        val request = createRequest()
        val response = createSuccessResponse(request)
        
        var interceptedRequest: Request? = null
        val chain = object : Interceptor.Chain {
            override fun request(): Request = request
            override fun proceed(request: Request): Response {
                interceptedRequest = request
                return response
            }
            override fun connection(): Connection? = null
            override fun call(): Call = throw NotImplementedError()
            override fun connectTimeoutMillis(): Int = 0
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun readTimeoutMillis(): Int = 0
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun writeTimeoutMillis(): Int = 0
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
        }

        // When
        val result = interceptor.intercept(chain)

        // Then
        assertNotNull(interceptedRequest)
        assertEquals(200, result.code)
    }

    @Test
    fun `HeaderInterceptor replaces existing header with same name`() {
        // Given
        val headers = mapOf("X-Test" to "NewValue")
        val interceptor = HeaderInterceptor(headers)
        val request = Request.Builder()
            .url("https://api.example.com/test")
            .header("X-Test", "OldValue")
            .build()
        val response = createSuccessResponse(request)
        
        var interceptedRequest: Request? = null
        val chain = object : Interceptor.Chain {
            override fun request(): Request = request
            override fun proceed(request: Request): Response {
                interceptedRequest = request
                return response
            }
            override fun connection(): Connection? = null
            override fun call(): Call = throw NotImplementedError()
            override fun connectTimeoutMillis(): Int = 0
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun readTimeoutMillis(): Int = 0
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun writeTimeoutMillis(): Int = 0
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
        }

        // When
        interceptor.intercept(chain)

        // Then
        assertNotNull(interceptedRequest)
        assertEquals("NewValue", interceptedRequest!!.header("X-Test"))
    }

    // ========== RetryInterceptor Tests ==========

    @Test
    fun `RetryInterceptor returns successful response without retry`() {
        // Given
        val interceptor = RetryInterceptor(maxRetries = 3)
        val request = createRequest()
        val response = createSuccessResponse(request)
        
        var proceedCount = 0
        val chain = object : Interceptor.Chain {
            override fun request(): Request = request
            override fun proceed(request: Request): Response {
                proceedCount++
                return response
            }
            override fun connection(): Connection? = null
            override fun call(): Call = throw NotImplementedError()
            override fun connectTimeoutMillis(): Int = 0
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun readTimeoutMillis(): Int = 0
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun writeTimeoutMillis(): Int = 0
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
        }

        // When
        val result = interceptor.intercept(chain)

        // Then
        assertEquals(200, result.code)
        assertEquals(1, proceedCount) // Should only proceed once
    }

    @Test
    fun `RetryInterceptor does not retry client errors (4xx)`() {
        // Given
        val interceptor = RetryInterceptor(maxRetries = 3)
        val request = createRequest()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(404)
            .message("Not Found")
            .body("".toResponseBody("application/json".toMediaTypeOrNull()))
            .build()
        
        var proceedCount = 0
        val chain = object : Interceptor.Chain {
            override fun request(): Request = request
            override fun proceed(request: Request): Response {
                proceedCount++
                return response
            }
            override fun connection(): Connection? = null
            override fun call(): Call = throw NotImplementedError()
            override fun connectTimeoutMillis(): Int = 0
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun readTimeoutMillis(): Int = 0
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun writeTimeoutMillis(): Int = 0
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
        }

        // When
        val result = interceptor.intercept(chain)

        // Then
        assertEquals(404, result.code)
        assertEquals(1, proceedCount) // Should not retry
    }

    @Test
    fun `RetryInterceptor calculates backoff time correctly`() {
        // Given
        val interceptor = RetryInterceptor(maxRetries = 3)
        
        // Use reflection to access private method for testing
        val method = RetryInterceptor::class.java.getDeclaredMethod("calculateBackoffTime", Int::class.java)
        method.isAccessible = true

        // When/Then
        assertEquals(100L, method.invoke(interceptor, 0))  // 100ms * 2^0
        assertEquals(200L, method.invoke(interceptor, 1))  // 100ms * 2^1
        assertEquals(400L, method.invoke(interceptor, 2))  // 100ms * 2^2
        assertEquals(800L, method.invoke(interceptor, 3))  // 100ms * 2^3
    }

    @Test(expected = IOException::class)
    fun `RetryInterceptor throws IOException after max retries`() {
        // Given
        val interceptor = RetryInterceptor(maxRetries = 2)
        val request = createRequest()
        
        var proceedCount = 0
        val chain = object : Interceptor.Chain {
            override fun request(): Request = request
            override fun proceed(request: Request): Response {
                proceedCount++
                throw IOException("Network error")
            }
            override fun connection(): Connection? = null
            override fun call(): Call = throw NotImplementedError()
            override fun connectTimeoutMillis(): Int = 0
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun readTimeoutMillis(): Int = 0
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
            override fun writeTimeoutMillis(): Int = 0
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
        }

        // When/Then - Should throw IOException after retries
        interceptor.intercept(chain)
    }
}
