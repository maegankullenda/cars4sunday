package com.maegankullenda.carsonsunday.ui.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maegankullenda.carsonsunday.domain.model.Event
import com.maegankullenda.carsonsunday.domain.model.EventStatus
import com.maegankullenda.carsonsunday.domain.model.UserRole
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun eventsScreen(
    onNavigateToCreateEvent: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToEventDetail: (String) -> Unit,
    viewModel: EventsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isAdmin = currentUser?.role == UserRole.ADMIN

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Events") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = onNavigateToCreateEvent) {
                            Icon(Icons.Default.Add, contentDescription = "Create Event")
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = EventStatus.values().indexOf(selectedTab),
            ) {
                EventStatus.values().forEach { status ->
                    Tab(
                        selected = selectedTab == status,
                        onClick = { viewModel.selectTab(status) },
                        text = { Text(status.name) },
                    )
                }
            }

            // Content based on selected tab
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            ) {
                when (val state = uiState) {
                    is EventsUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                    is EventsUiState.Success -> {
                        if (state.events.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = "No ${selectedTab.name.lowercase()} events found",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                if (isAdmin && selectedTab == EventStatus.UPCOMING) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Tap the + button to create your first event",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(state.events) { event ->
                                    eventCard(event = event, onClick = { onNavigateToEventDetail(event.id) })
                                }
                            }
                        }
                    }
                    is EventsUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = "Error: ${state.message}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun eventCard(
    event: Event,
    onClick: () -> Unit,
    viewModel: EventsViewModel = hiltViewModel(),
) {
    val dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val attendanceStatus by viewModel.attendanceStatus.collectAsStateWithLifecycle()
    val isUserAttending = attendanceStatus[event.id] ?: false

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Date: ${event.date.format(dateFormatter)}",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Location: ${event.location}",
                style = MaterialTheme.typography.bodySmall,
            )

            // Attendance information
            if (event.status == EventStatus.UPCOMING) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Attendee count display
                    Text(
                        text = when {
                            event.attendeeLimit != null ->
                                "${event.attendeeCount}/${event.attendeeLimit} attendees"
                            else ->
                                "${event.attendeeCount} attendees"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (event.isAtCapacity) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    )

                    // Attendance button
                    if (currentUser != null) {
                        Button(
                            onClick = {
                                if (isUserAttending) {
                                    viewModel.leaveEvent(event.id)
                                } else {
                                    viewModel.attendEvent(event.id)
                                }
                            },
                            enabled = !event.isAtCapacity || isUserAttending,
                        ) {
                            Text(
                                text = when {
                                    isUserAttending -> "Leave"
                                    event.isAtCapacity -> "Full"
                                    else -> "Attend"
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
