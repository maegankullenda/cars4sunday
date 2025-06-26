package com.maegankullenda.carsonsunday.data.repository.impl

import com.maegankullenda.carsonsunday.data.source.local.UserLocalDataSource
import com.maegankullenda.carsonsunday.domain.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private lateinit var authRepository: AuthRepositoryImpl
    private lateinit var mockUserLocalDataSource: UserLocalDataSource

    @Before
    fun setUp() {
        mockUserLocalDataSource = mockk()
        authRepository = AuthRepositoryImpl(mockUserLocalDataSource)
    }

    @Test
    fun `login with valid credentials should return success`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val user = User(
            id = "1",
            username = username,
            password = password,
            name = "Test",
            surname = "User",
            mobileNumber = "1234567890",
        )
        coEvery { mockUserLocalDataSource.getUser() } returns user

        // When
        val result = authRepository.login(username, password)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
    }

    @Test
    fun `login with invalid username should return failure`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val user = User(
            id = "1",
            username = "differentuser",
            password = password,
            name = "Test",
            surname = "User",
            mobileNumber = "1234567890",
        )
        coEvery { mockUserLocalDataSource.getUser() } returns user

        // When
        val result = authRepository.login(username, password)

        // Then
        assertFalse(result.isSuccess)
        assertEquals("Invalid username or password", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login with invalid password should return failure`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val user = User(
            id = "1",
            username = username,
            password = "differentpass",
            name = "Test",
            surname = "User",
            mobileNumber = "1234567890",
        )
        coEvery { mockUserLocalDataSource.getUser() } returns user

        // When
        val result = authRepository.login(username, password)

        // Then
        assertFalse(result.isSuccess)
        assertEquals("Invalid username or password", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login with no stored user should return failure`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        coEvery { mockUserLocalDataSource.getUser() } returns null

        // When
        val result = authRepository.login(username, password)

        // Then
        assertFalse(result.isSuccess)
        assertEquals("Invalid username or password", result.exceptionOrNull()?.message)
    }

    @Test
    fun `register with new user should return success`() = runTest {
        // Given
        val username = "newuser"
        val password = "newpass"
        val name = "New"
        val surname = "User"
        val mobileNumber = "1234567890"

        coEvery { mockUserLocalDataSource.getUser() } returns null
        coEvery { mockUserLocalDataSource.saveUser(any()) } returns Unit

        // When
        val result = authRepository.register(username, password, name, surname, mobileNumber)

        // Then
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals(username, user?.username)
        assertEquals(password, user?.password)
        assertEquals(name, user?.name)
        assertEquals(surname, user?.surname)
        assertEquals(mobileNumber, user?.mobileNumber)
        assertNotNull(user?.id)

        coVerify { mockUserLocalDataSource.saveUser(user!!) }
    }

    @Test
    fun `register with existing username should return failure`() = runTest {
        // Given
        val username = "existinguser"
        val password = "newpass"
        val name = "New"
        val surname = "User"
        val mobileNumber = "1234567890"

        val existingUser = User(
            id = "1",
            username = username,
            password = "oldpass",
            name = "Old",
            surname = "User",
            mobileNumber = "0987654321",
        )
        coEvery { mockUserLocalDataSource.getUser() } returns existingUser

        // When
        val result = authRepository.register(username, password, name, surname, mobileNumber)

        // Then
        assertFalse(result.isSuccess)
        assertEquals("Username already exists", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getCurrentUser should return stored user`() = runTest {
        // Given
        val expectedUser = User(
            id = "1",
            username = "testuser",
            password = "testpass",
            name = "Test",
            surname = "User",
            mobileNumber = "1234567890",
        )
        coEvery { mockUserLocalDataSource.getUser() } returns expectedUser

        // When
        val result = authRepository.getCurrentUser()

        // Then
        assertEquals(expectedUser, result)
    }

    @Test
    fun `getCurrentUser should return null when no user stored`() = runTest {
        // Given
        coEvery { mockUserLocalDataSource.getUser() } returns null

        // When
        val result = authRepository.getCurrentUser()

        // Then
        assertNull(result)
    }

    @Test
    fun `logout should clear user data`() = runTest {
        // Given
        coEvery { mockUserLocalDataSource.clearUser() } returns Unit

        // When
        authRepository.logout()

        // Then
        coVerify { mockUserLocalDataSource.clearUser() }
    }

    @Test
    fun `isUserLoggedIn should return flow from data source`() {
        // Given
        val expectedFlow = flowOf(true)
        every { mockUserLocalDataSource.isUserLoggedIn } returns expectedFlow

        // When
        val result = authRepository.isUserLoggedIn()

        // Then
        assertEquals(expectedFlow, result)
    }
}
