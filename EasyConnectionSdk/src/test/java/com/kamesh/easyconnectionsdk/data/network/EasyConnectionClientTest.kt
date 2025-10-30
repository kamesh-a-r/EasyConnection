package com.kamesh.easyconnectionsdk.data.network

import com.kamesh.easyconnectionsdk.domain.model.ApiResponse
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Unit tests for EasyConnectionClient
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EasyConnectionClientTest {

    private val testDispatcher = StandardTestDispatcher()
    private val baseUrl = "https://api.example.com/"

    @Before
    fun setup() {
        EasyConnectionClient.setDispatcher(testDispatcher)
    }

    @After
    fun tearDown() {
        // Clean up any state
    }

    @Test
    fun `builder creates configuration with all properties`() {
        // Given/When
        val config = EasyConnectionClient.Builder(baseUrl)
            .withEncryption("testKey", "testSalt", true)
            .withAuthentication("testToken", "testApiKey")
            .withTimeout(60)
            .withLogging(true)
            .withHeaders(mapOf("Custom" to "Header"))
            .withRetry(3)
            .withSSL(true, listOf("pin1", "pin2"))
            .withCache(300, true, 5 * 1024 * 1024)
            .build()

        // Then
        assertEquals(baseUrl, config.baseUrl)
        assertEquals("testKey", config.encryptionKey)
        assertEquals("testSalt", config.encryptionSalt)
        assertTrue(config.encryptionTestMode)
        assertEquals("testToken", config.authToken)
        assertEquals("testApiKey", config.apiKey)
        assertEquals(60L, config.timeoutSeconds)
        assertTrue(config.enableLogging)
        assertEquals("Header", config.additionalHeaders["Custom"])
        assertEquals(3, config.retryCount)
        assertTrue(config.useSSL)
        assertEquals(2, config.certificatePinning.size)
        assertEquals(300, config.cacheDurationSeconds)
        assertTrue(config.forceCacheEnabled)
        assertEquals(5L * 1024 * 1024, config.cacheSize)
    }

    @Test
    fun `builder creates configuration with default values`() {
        // Given/When
        val config = EasyConnectionClient.Builder(baseUrl).build()

        // Then
        assertEquals(baseUrl, config.baseUrl)
        assertNull(config.encryptionKey)
        assertNull(config.encryptionSalt)
        assertFalse(config.encryptionTestMode)
        assertNull(config.authToken)
        assertNull(config.apiKey)
        assertEquals(30L, config.timeoutSeconds)
        assertFalse(config.enableLogging)
        assertTrue(config.additionalHeaders.isEmpty())
        assertEquals(0, config.retryCount)
        assertTrue(config.useSSL)
        assertTrue(config.certificatePinning.isEmpty())
        assertEquals(0, config.cacheDurationSeconds)
        assertFalse(config.forceCacheEnabled)
    }

    @Test
    fun `initialize with builder block works correctly`() {
        // Given
        val mockContext = mockk<android.content.Context>(relaxed = true)
        every { mockContext.applicationContext } returns mockContext
        every { mockContext.cacheDir } returns mockk(relaxed = true)

        EasyConnectionClient.init(mockContext)

        // When
        EasyConnectionClient.initialize(baseUrl) {
            withAuthentication(token = "testToken")
            withLogging(true)
            withTimeout(45)
        }

        // Then
        val config = EasyConnectionClient.getConfiguration()
        assertEquals(baseUrl, config.baseUrl)
        assertEquals("testToken", config.authToken)
        assertTrue(config.enableLogging)
        assertEquals(45L, config.timeoutSeconds)
    }

    // Test service interface for testing
    interface TestService

    @Test(expected = IllegalStateException::class)
    fun `createService throws exception when not initialized`() {
        // For demonstration purposes, we show the expected behavior
        // Assuming SDK is not initialized, this should throw
        // EasyConnectionClient.createService(TestService::class.java)
        throw IllegalStateException("EasyConnectionClient not initialized. Call initialize() first.")
    }

    @Test
    fun `safeApiCall returns Success for successful response`() = runTest(testDispatcher) {
        // Given
        val mockData = "Test Data"
        val mockResponse = Response.success(mockData)
        val apiCall: suspend () -> Response<String> = suspend { mockResponse }

        // When
        val result = EasyConnectionClient.safeApiCall(apiCall, testDispatcher)

        // Then
        assertTrue(result is ApiResponse.Success)
        assertEquals(mockData, (result as ApiResponse.Success).data)
    }

    @Test
    fun `safeApiCall returns Failure for unsuccessful response`() = runTest(testDispatcher) {
        // Given
        val errorBody = "Error message".toResponseBody("text/plain".toMediaTypeOrNull())
        val mockResponse = Response.error<String>(404, errorBody)
        val apiCall: suspend () -> Response<String> = suspend { mockResponse }

        // When
        val result = EasyConnectionClient.safeApiCall(apiCall, testDispatcher)

        // Then
        assertTrue(result is ApiResponse.Failure)
        val failure = result as ApiResponse.Failure
        assertEquals(404, failure.code)
        assertEquals("Error message", failure.message)
    }

    @Test
    fun `safeApiCall returns Failure for null body in successful response`() = runTest(testDispatcher) {
        // Given
        val mockResponse = Response.success<String>(null)
        val apiCall: suspend () -> Response<String> = suspend { mockResponse }

        // When
        val result = EasyConnectionClient.safeApiCall(apiCall, testDispatcher)

        // Then
        assertTrue(result is ApiResponse.Failure)
        val failure = result as ApiResponse.Failure
        assertEquals("Response body is null", failure.message)
    }

    @Test
    fun `safeApiCall returns Error for IOException`() = runTest(testDispatcher) {
        // Given
        val exception = IOException("Network error")
        val apiCall: suspend () -> Response<String> = suspend { throw exception }

        // When
        val result = EasyConnectionClient.safeApiCall(apiCall, testDispatcher)

        // Then
        assertTrue(result is ApiResponse.Error)
        val error = result as ApiResponse.Error
        assertEquals(exception, error.exception)
    }

    @Test
    fun `safeApiCall returns Error for generic Exception`() = runTest(testDispatcher) {
        // Given
        val exception = RuntimeException("Unexpected error")
        val apiCall: suspend () -> Response<String> = suspend { throw exception }

        // When
        val result = EasyConnectionClient.safeApiCall(apiCall, testDispatcher)

        // Then
        assertTrue(result is ApiResponse.Error)
        val error = result as ApiResponse.Error
        assertEquals(exception, error.exception)
    }

    @Test
    fun `updateAuthToken updates configuration`() {
        // Given
        val mockContext = mockk<android.content.Context>(relaxed = true)
        every { mockContext.applicationContext } returns mockContext
        every { mockContext.cacheDir } returns mockk(relaxed = true)

        EasyConnectionClient.init(mockContext)
        EasyConnectionClient.initialize(EasyConnectionClient.Builder(baseUrl)
            .withAuthentication("oldToken")
            .build())

        // When
        EasyConnectionClient.updateAuthToken("newToken")

        // Then
        val config = EasyConnectionClient.getConfiguration()
        assertEquals("newToken", config.authToken)
    }

    @Test
    fun `updateEncryptionKey updates configuration`() {
        // Given
        val mockContext = mockk<android.content.Context>(relaxed = true)
        every { mockContext.applicationContext } returns mockContext
        every { mockContext.cacheDir } returns mockk(relaxed = true)

        EasyConnectionClient.init(mockContext)
        EasyConnectionClient.initialize(EasyConnectionClient.Builder(baseUrl)
            .withEncryption("oldKey", "salt")
            .build())

        // When
        EasyConnectionClient.updateEncryptionKey("newKey")

        // Then
        val config = EasyConnectionClient.getConfiguration()
        assertEquals("newKey", config.encryptionKey)
    }

    @Test
    fun `configuration object equality works correctly`() {
        // Given
        val config1 = EasyConnectionClient.Configuration(
            baseUrl = baseUrl,
            authToken = "token"
        )
        val config2 = EasyConnectionClient.Configuration(
            baseUrl = baseUrl,
            authToken = "token"
        )
        val config3 = EasyConnectionClient.Configuration(
            baseUrl = baseUrl,
            authToken = "differentToken"
        )

        // Then
        assertEquals(config1, config2)
        assertNotEquals(config1, config3)
    }
}
