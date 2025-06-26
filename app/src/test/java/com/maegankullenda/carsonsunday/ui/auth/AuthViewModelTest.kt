package com.maegankullenda.carsonsunday.ui.auth

import com.maegankullenda.carsonsunday.domain.model.User
import com.maegankullenda.carsonsunday.domain.usecase.LoginUseCase
import com.maegankullenda.carsonsunday.domain.usecase.RegisterUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var mockLoginUseCase: LoginUseCase
    private lateinit var mockRegisterUseCase: RegisterUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockLoginUseCase = mockk()
        mockRegisterUseCase = mockk()
        authViewModel = AuthViewModel(
            loginUseCase = mockLoginUseCase,
            registerUseCase = mockRegisterUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Initial`() = runTest {
        // Then
        assertTrue(authViewModel.uiState.value is AuthUiState.Initial)
    }

    @Test
    fun `login with valid credentials should update state to Success`() = runTest {
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
        coEvery { mockLoginUseCase(username, password) } returns Result.success(user)

        // When
        authViewModel.login(username, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = authViewModel.uiState.value
        assertTrue(state is AuthUiState.Success)
        assertEquals(user, (state as AuthUiState.Success).user)
    }

    @Test
    fun `login with invalid credentials should update state to Error`() = runTest {
        // Given
        val username = "testuser"
        val password = "wrongpass"
        val errorMessage = "Invalid username or password"
        coEvery { mockLoginUseCase(username, password) } returns Result.failure(Exception(errorMessage))

        // When
        authViewModel.login(username, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = authViewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals(errorMessage, (state as AuthUiState.Error).message)
    }

    @Test
    fun `register with valid data should update state to Success`() = runTest {
        // Given
        val username = "newuser"
        val password = "newpass"
        val name = "New"
        val surname = "User"
        val mobileNumber = "1234567890"
        val user = User(
            id = "1",
            username = username,
            password = password,
            name = name,
            surname = surname,
            mobileNumber = mobileNumber,
        )
        coEvery {
            mockRegisterUseCase(username, password, name, surname, mobileNumber)
        } returns Result.success(user)

        // When
        authViewModel.register(username, password, name, surname, mobileNumber)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = authViewModel.uiState.value
        assertTrue(state is AuthUiState.Success)
        assertEquals(user, (state as AuthUiState.Success).user)
    }

    @Test
    fun `register with invalid data should update state to Error`() = runTest {
        // Given
        val username = "newuser"
        val password = "newpass"
        val name = "New"
        val surname = "User"
        val mobileNumber = "1234567890"
        val errorMessage = "Username already exists"
        coEvery {
            mockRegisterUseCase(username, password, name, surname, mobileNumber)
        } returns Result.failure(Exception(errorMessage))

        // When
        authViewModel.register(username, password, name, surname, mobileNumber)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = authViewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals(errorMessage, (state as AuthUiState.Error).message)
    }

    @Test
    fun `resetState should reset to Initial state`() = runTest {
        // Given
        val username = "testuser"
        val password = "wrongpass"
        val errorMessage = "Invalid credentials"
        coEvery { mockLoginUseCase(username, password) } returns Result.failure(Exception(errorMessage))

        authViewModel.login(username, password)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(authViewModel.uiState.value is AuthUiState.Error)

        // When
        authViewModel.resetState()

        // Then
        assertTrue(authViewModel.uiState.value is AuthUiState.Initial)
    }
}
