package com.maegankullenda.carsonsunday.ui.notices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maegankullenda.carsonsunday.domain.model.NoticePriority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun createNoticeScreen(
    onNoticeCreated: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: CreateNoticeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(NoticePriority.MEDIUM) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CreateNoticeUiState.Success -> {
                viewModel.resetState()
                onNoticeCreated()
            }
            is CreateNoticeUiState.Error -> {
                errorMessage = state.message
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Notice") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            noticePriorityDropdown(priority = priority, onPriorityChange = { priority = it })
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.createNotice(title, content, priority)
                },
                enabled = uiState !is CreateNoticeUiState.Loading,
            ) {
                Text("Create Notice")
            }
            if (uiState is CreateNoticeUiState.Loading) {
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator()
            }
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun noticePriorityDropdown(priority: NoticePriority, onPriorityChange: (NoticePriority) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("Priority: ${priority.name}")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            NoticePriority.values().forEach { value ->
                DropdownMenuItem(
                    text = { Text(value.name) },
                    onClick = {
                        onPriorityChange(value)
                        expanded = false
                    },
                )
            }
        }
    }
}
