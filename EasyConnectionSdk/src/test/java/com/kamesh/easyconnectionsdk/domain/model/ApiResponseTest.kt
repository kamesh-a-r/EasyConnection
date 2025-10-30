package com.kamesh.easyconnectionsdk.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

/**
 * Unit tests for ApiResponse sealed class
 */
class ApiResponseTest {

    @Test
    fun `Success response returns correct data`() {
        // Given
        val data = "Test Data"
        val response = ApiResponse.Success(data)

        // Then
        assertEquals(data, response.data)
        assertTrue(response.isSuccess)
    }

    @Test
    fun `Failure response contains code and message`() {
        // Given
        val code = 404
        val message = "Not Found"
        val response = ApiResponse.Failure(code, message)

        // Then
        assertEquals(code, response.code)
        assertEquals(message, response.message)
        assertFalse(response.isSuccess)
    }

    @Test
    fun `Error response contains exception`() {
        // Given
        val exception = IOException("Network error")
        val response = ApiResponse.Error(exception)

        // Then
        assertEquals(exception, response.exception)
        assertFalse(response.isSuccess)
    }

    @Test
    fun `getOrNull returns data for Success`() {
        // Given
        val data = "Test Data"
        val response = ApiResponse.Success(data)

        // When
        val result = response.getOrNull()

        // Then
        assertEquals(data, result)
    }

    @Test
    fun `getOrNull returns null for Failure`() {
        // Given
        val response = ApiResponse.Failure(404, "Not Found")

        // When
        val result = response.getOrNull()

        // Then
        assertNull(result)
    }

    @Test
    fun `getOrNull returns null for Error`() {
        // Given
        val response = ApiResponse.Error(IOException())

        // When
        val result = response.getOrNull()

        // Then
        assertNull(result)
    }

    @Test
    fun `map transforms Success data`() {
        // Given
        val response = ApiResponse.Success(5)

        // When
        val mapped = response.map { it * 2 }

        // Then
        assertTrue(mapped is ApiResponse.Success)
        assertEquals(10, (mapped as ApiResponse.Success).data)
    }

    @Test
    fun `map preserves Failure`() {
        // Given
        val response: ApiResponse<Int> = ApiResponse.Failure(404, "Not Found")

        // When
        val mapped = response.map { it * 2 }

        // Then
        assertTrue(mapped is ApiResponse.Failure)
        assertEquals(404, (mapped as ApiResponse.Failure).code)
    }

    @Test
    fun `map preserves Error`() {
        // Given
        val exception = IOException()
        val response: ApiResponse<Int> = ApiResponse.Error(exception)

        // When
        val mapped = response.map { it * 2 }

        // Then
        assertTrue(mapped is ApiResponse.Error)
        assertEquals(exception, (mapped as ApiResponse.Error).exception)
    }

    @Test
    fun `onSuccess executes action for Success`() {
        // Given
        val response = ApiResponse.Success("data")
        var actionExecuted = false

        // When
        response.onSuccess { actionExecuted = true }

        // Then
        assertTrue(actionExecuted)
    }

    @Test
    fun `onSuccess does not execute action for Failure`() {
        // Given
        val response: ApiResponse<String> = ApiResponse.Failure(404, "Not Found")
        var actionExecuted = false

        // When
        response.onSuccess { actionExecuted = true }

        // Then
        assertFalse(actionExecuted)
    }

    @Test
    fun `onSuccess does not execute action for Error`() {
        // Given
        val response: ApiResponse<String> = ApiResponse.Error(IOException())
        var actionExecuted = false

        // When
        response.onSuccess { actionExecuted = true }

        // Then
        assertFalse(actionExecuted)
    }

    @Test
    fun `onFailure executes action for Failure`() {
        // Given
        val response = ApiResponse.Failure(404, "Not Found")
        var capturedCode: Int? = null
        var capturedMessage: String? = null

        // When
        response.onFailure { code, message ->
            capturedCode = code
            capturedMessage = message
        }

        // Then
        assertEquals(404, capturedCode)
        assertEquals("Not Found", capturedMessage)
    }

    @Test
    fun `onFailure does not execute action for Success`() {
        // Given
        val response = ApiResponse.Success("data")
        var actionExecuted = false

        // When
        response.onFailure { _, _ -> actionExecuted = true }

        // Then
        assertFalse(actionExecuted)
    }

    @Test
    fun `onError executes action for Error`() {
        // Given
        val exception = IOException("Network error")
        val response = ApiResponse.Error(exception)
        var capturedError: Throwable? = null

        // When
        response.onError { capturedError = it }

        // Then
        assertEquals(exception, capturedError)
    }

    @Test
    fun `onError does not execute action for Success`() {
        // Given
        val response = ApiResponse.Success("data")
        var actionExecuted = false

        // When
        response.onError { actionExecuted = true }

        // Then
        assertFalse(actionExecuted)
    }

    @Test
    fun `onFailureOrError executes for Failure`() {
        // Given
        val response = ApiResponse.Failure(404, "Not Found")
        var actionExecuted = false

        // When
        response.onFailureOrError { actionExecuted = true }

        // Then
        assertTrue(actionExecuted)
    }

    @Test
    fun `onFailureOrError executes for Error`() {
        // Given
        val response = ApiResponse.Error(IOException())
        var actionExecuted = false

        // When
        response.onFailureOrError { actionExecuted = true }

        // Then
        assertTrue(actionExecuted)
    }

    @Test
    fun `onFailureOrError does not execute for Success`() {
        // Given
        val response = ApiResponse.Success("data")
        var actionExecuted = false

        // When
        response.onFailureOrError { actionExecuted = true }

        // Then
        assertFalse(actionExecuted)
    }

    @Test
    fun `getOrThrow returns data for Success`() {
        // Given
        val data = "Test Data"
        val response = ApiResponse.Success(data)

        // When
        val result = response.getOrThrow()

        // Then
        assertEquals(data, result)
    }

    @Test(expected = RuntimeException::class)
    fun `getOrThrow throws RuntimeException for Failure`() {
        // Given
        val response = ApiResponse.Failure(404, "Not Found")

        // When/Then
        response.getOrThrow()
    }

    @Test(expected = IOException::class)
    fun `getOrThrow throws original exception for Error`() {
        // Given
        val response = ApiResponse.Error(IOException("Network error"))

        // When/Then
        response.getOrThrow()
    }

    @Test
    fun `getOrDefault returns data for Success`() {
        // Given
        val data = "Test Data"
        val response = ApiResponse.Success(data)

        // When
        val result = response.getOrDefault("Default")

        // Then
        assertEquals(data, result)
    }

    @Test
    fun `getOrDefault returns default for Failure`() {
        // Given
        val response: ApiResponse<String> = ApiResponse.Failure(404, "Not Found")
        val defaultValue = "Default"

        // When
        val result = response.getOrDefault(defaultValue)

        // Then
        assertEquals(defaultValue, result)
    }

    @Test
    fun `getOrDefault returns default for Error`() {
        // Given
        val response: ApiResponse<String> = ApiResponse.Error(IOException())
        val defaultValue = "Default"

        // When
        val result = response.getOrDefault(defaultValue)

        // Then
        assertEquals(defaultValue, result)
    }

    @Test
    fun `chaining handlers works correctly for Success`() {
        // Given
        val response = ApiResponse.Success("data")
        var successCalled = false
        var failureCalled = false
        var errorCalled = false

        // When
        response
            .onSuccess { successCalled = true }
            .onFailure { _, _ -> failureCalled = true }
            .onError { errorCalled = true }

        // Then
        assertTrue(successCalled)
        assertFalse(failureCalled)
        assertFalse(errorCalled)
    }

    @Test
    fun `chaining handlers works correctly for Failure`() {
        // Given
        val response: ApiResponse<String> = ApiResponse.Failure(404, "Not Found")
        var successCalled = false
        var failureCalled = false
        var errorCalled = false

        // When
        response
            .onSuccess { successCalled = true }
            .onFailure { _, _ -> failureCalled = true }
            .onError { errorCalled = true }

        // Then
        assertFalse(successCalled)
        assertTrue(failureCalled)
        assertFalse(errorCalled)
    }

    @Test
    fun `chaining handlers works correctly for Error`() {
        // Given
        val response: ApiResponse<String> = ApiResponse.Error(IOException())
        var successCalled = false
        var failureCalled = false
        var errorCalled = false

        // When
        response
            .onSuccess { successCalled = true }
            .onFailure { _, _ -> failureCalled = true }
            .onError { errorCalled = true }

        // Then
        assertFalse(successCalled)
        assertFalse(failureCalled)
        assertTrue(errorCalled)
    }
}
