package com.maegankullenda.carsonsunday.ui.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun welcomeScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: WelcomeViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        when (val currentState = uiState) {
            is WelcomeUiState.LoggedOut -> {
                onNavigateToLogin()
            }
            is WelcomeUiState.Error -> {
                snackbarHostState.showSnackbar(currentState.message)
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when (val currentState = uiState) {
            is WelcomeUiState.Loading -> {
                CircularProgressIndicator()
            }
            is WelcomeUiState.Success -> {
                Text(
                    text = "Welcome ${currentState.user.name}",
                    style = MaterialTheme.typography.headlineMedium,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "You are successfully logged in!",
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Logout")
                }
            }
            is WelcomeUiState.Error -> {
                Text(
                    text = "Error: ${currentState.message}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onNavigateToLogin() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Go to Login")
                }
            }
            is WelcomeUiState.LoggedOut -> {
                // This will be handled by LaunchedEffect
            }
        }
    }

    SnackbarHost(hostState = snackbarHostState)
}
