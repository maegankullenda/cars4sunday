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

class RegisterUseCaseTest {

    private lateinit var registerUseCase: RegisterUseCase
    private lateinit var mockAuthRepository: AuthRepository

    @Before
    fun setUp() {
        mockAuthRepository = mockk()
        registerUseCase = RegisterUseCase(mockAuthRepository)
    }

    @Test
    fun `invoke with valid data should return success`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val name = "Test"
        val surname = "User"
        val mobileNumber = "1234567890"
        val expectedUser = User(
            id = "1",
            username = username,
            password = password,
            name = name,
            surname = surname,
            mobileNumber = mobileNumber,
        )
        coEvery {
            mockAuthRepository.register(username, password, name, surname, mobileNumber)
        } returns Result.success(expectedUser)

        // When
        val result = registerUseCase(
            username = username,
            password = password,
            name = name,
            surname = surname,
            mobileNumber = mobileNumber,
        )

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedUser, result.getOrNull())
    }

    @Test
    fun `invoke with blank username should return failure`() = runTest {
        // Given
        val username = ""
        val password = "testpass"
        val name = "Test"
        val surname = "User"
        val mobileNumber = "1234567890"

        // When
        val result = registerUseCase(
            username = username,
            password = password,
            name = name,
            surname = surname,
            mobileNumber = mobileNumber,
        )

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
        val name = "Test"
        val surname = "User"
        val mobileNumber = "1234567890"

        // When
        val result = registerUseCase(
            username = username,
            password = password,
            name = name,
            surname = surname,
            mobileNumber = mobileNumber,
        )

        // Then
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Password cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with blank name should return failure`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val name = ""
        val surname = "User"
        val mobileNumber = "1234567890"

        // When
        val result = registerUseCase(
            username = username,
            password = password,
            name = name,
            surname = surname,
            mobileNumber = mobileNumber,
        )

        // Then
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Name cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with blank surname should return failure`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val name = "Test"
        val surname = ""
        val mobileNumber = "1234567890"

        // When
        val result = registerUseCase(
            username = username,
            password = password,
            name = name,
            surname = surname,
            mobileNumber = mobileNumber,
        )

        // Then
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Surname cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with blank mobile number should return failure`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val name = "Test"
        val surname = "User"
        val mobileNumber = ""

        // When
        val result = registerUseCase(
            username = username,
            password = password,
            name = name,
            surname = surname,
            mobileNumber = mobileNumber,
        )

        // Then
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Mobile number cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with whitespace values should return failure`() = runTest {
        // Given
        val username = "   "
        val password = "   "
        val name = "   "
        val surname = "   "
        val mobileNumber = "   "

        // When
        val result = registerUseCase(
            username = username,
            password = password,
            name = name,
            surname = surname,
            mobileNumber = mobileNumber,
        )

        // Then
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Username cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke when repository returns failure should propagate failure`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val name = "Test"
        val surname = "User"
        val mobileNumber = "1234567890"
        val expectedException = Exception("Username already exists")
        coEvery {
            mockAuthRepository.register(username, password, name, surname, mobileNumber)
        } returns Result.failure(expectedException)

        // When
        val result = registerUseCase(
            username = username,
            password = password,
            name = name,
            surname = surname,
            mobileNumber = mobileNumber,
        )

        // Then
        assertFalse(result.isSuccess)
        assertEquals(expectedException, result.exceptionOrNull())
    }
}
