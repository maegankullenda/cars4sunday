package com.maegankullenda.carsonsunday.domain.usecase

import com.maegankullenda.carsonsunday.domain.model.User
import com.maegankullenda.carsonsunday.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginUseCaseTest {

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var mockAuthRepository: AuthRepository

    @Before
    fun setUp() {
        mockAuthRepository = mockk()
        loginUseCase = LoginUseCase(mockAuthRepository)
    }

    @Test
    fun `invoke with valid credentials should return success`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val expectedUser = User(
            id = "1",
            username = username,
            password = password,
            name = "Test",
            surname = "User",
            mobileNumber = "1234567890"
        )
        coEvery { mockAuthRepository.login(username, password) } returns Result.success(expectedUser)

        // When
        val result = loginUseCase(username, password)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedUser, result.getOrNull())
    }

    @Test
    fun `invoke with blank username should return failure`() = runTest {
        // Given
        val username = ""
        val password = "testpass"

        // When
        val result = loginUseCase(username, password)

        // Then
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Username cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with whitespace username should return failure`() = runTest {
        // Given
        val username = "   "
        val password = "testpass"

        // When
        val result = loginUseCase(username, password)

        // Then
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Username cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with blank password should return failure`() = runTest {
        // Given
        val username = "testuser"
        val password = ""

        // When
        val result = loginUseCase(username, password)

        // Then
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Password cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with whitespace password should return failure`() = runTest {
        // Given
        val username = "testuser"
        val password = "   "

        // When
        val result = loginUseCase(username, password)

        // Then
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Password cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke when repository returns failure should propagate failure`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val expectedException = Exception("Invalid credentials")
        coEvery { mockAuthRepository.login(username, password) } returns Result.failure(expectedException)

        // When
        val result = loginUseCase(username, password)

        // Then
        assertFalse(result.isSuccess)
        assertEquals(expectedException, result.exceptionOrNull())
    }
} 