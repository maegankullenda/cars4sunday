package com.maegankullenda.carsonsunday.ui.welcome

import com.maegankullenda.carsonsunday.domain.model.User
import com.maegankullenda.carsonsunday.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
class WelcomeViewModelTest {

    private lateinit var welcomeViewModel: WelcomeViewModel
    private lateinit var mockAuthRepository: AuthRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockAuthRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        // Given
        coEvery { mockAuthRepository.getCurrentUser() } returns null

        // When
        welcomeViewModel = WelcomeViewModel(authRepository = mockAuthRepository)

        // Then
        assertTrue(welcomeViewModel.uiState.value is WelcomeUiState.Loading)
    }

    @Test
    fun `loadCurrentUser with valid user should update state to Success`() = runTest {
        // Given
        val user = User(
            id = "1",
            username = "testuser",
            password = "testpass",
            name = "Test",
            surname = "User",
            mobileNumber = "1234567890",
        )
        coEvery { mockAuthRepository.getCurrentUser() } returns user

        // When
        welcomeViewModel = WelcomeViewModel(authRepository = mockAuthRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = welcomeViewModel.uiState.value
        assertTrue(state is WelcomeUiState.Success)
        assertEquals(user, (state as WelcomeUiState.Success).user)
    }

    @Test
    fun `loadCurrentUser with no user should update state to Error`() = runTest {
        // Given
        coEvery { mockAuthRepository.getCurrentUser() } returns null

        // When
        welcomeViewModel = WelcomeViewModel(authRepository = mockAuthRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = welcomeViewModel.uiState.value
        assertTrue(state is WelcomeUiState.Error)
        assertEquals("No user found", (state as WelcomeUiState.Error).message)
    }

    @Test
    fun `logout should update state to LoggedOut`() = runTest {
        // Given
        val user = User(
            id = "1",
            username = "testuser",
            password = "testpass",
            name = "Test",
            surname = "User",
            mobileNumber = "1234567890",
        )
        coEvery { mockAuthRepository.getCurrentUser() } returns user
        coEvery { mockAuthRepository.logout() } returns Unit

        // When
        welcomeViewModel = WelcomeViewModel(authRepository = mockAuthRepository)
        testDispatcher.scheduler.advanceUntilIdle() // Load user first
        welcomeViewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { mockAuthRepository.logout() }
        assertTrue(welcomeViewModel.uiState.value is WelcomeUiState.LoggedOut)
    }

    @Test
    fun `user display name should be formatted correctly`() = runTest {
        // Given
        val user = User(
            id = "1",
            username = "testuser",
            password = "testpass",
            name = "John",
            surname = "Doe",
            mobileNumber = "1234567890",
        )
        coEvery { mockAuthRepository.getCurrentUser() } returns user

        // When
        welcomeViewModel = WelcomeViewModel(authRepository = mockAuthRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = welcomeViewModel.uiState.value
        assertTrue(state is WelcomeUiState.Success)
        val loadedUser = (state as WelcomeUiState.Success).user
        assertEquals("John Doe", "${loadedUser.name} ${loadedUser.surname}")
    }
}
