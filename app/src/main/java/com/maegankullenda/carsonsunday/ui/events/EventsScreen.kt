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
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleStartEffect
import com.maegankullenda.carsonsunday.domain.model.Event
import com.maegankullenda.carsonsunday.domain.model.EventStatus
import com.maegankullenda.carsonsunday.domain.model.UserRole
import com.maegankullenda.carsonsunday.ui.components.CalendarPermissionDialog
import com.maegankullenda.carsonsunday.ui.components.GoogleAccountDialog
import com.maegankullenda.carsonsunday.ui.components.LocalPermissionHandler
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
    val hasCalendarPermission by viewModel.hasCalendarPermissionState.collectAsStateWithLifecycle()
    val hasCalendarAccount by viewModel.hasCalendarAccountState.collectAsStateWithLifecycle()
    val isAdmin = currentUser?.role == UserRole.ADMIN
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showGoogleAccountDialog by remember { mutableStateOf(false) }
    var permissionDialogDismissed by remember { mutableStateOf(false) }
    var googleAccountDialogDismissed by remember { mutableStateOf(false) }

    // Refresh calendar integration state on resume (e.g., after returning from Settings)
    LifecycleStartEffect(Unit) {
        viewModel.refreshCalendarIntegrationState()
        onStopOrDispose { }
    }

    // Reset dismissed flags if conditions change
    if (hasCalendarPermission) {
        permissionDialogDismissed = false
    }
    if (hasCalendarAccount) {
        googleAccountDialogDismissed = false
    }

    // Show permission dialog if needed
    if (!hasCalendarPermission && !showPermissionDialog && !permissionDialogDismissed) {
        showPermissionDialog = true
    }

    // Show Google account dialog if needed (only after permission granted)
    if (hasCalendarPermission && !hasCalendarAccount && !showGoogleAccountDialog && !googleAccountDialogDismissed) {
        showGoogleAccountDialog = true
    }

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
                    IconButton(onClick = { viewModel.refreshEvents() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Events")
                    }
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

            // Temporary test notification button (remove after Cloud Functions are deployed)
            if (isAdmin) {
                Button(
                    onClick = { viewModel.testFCMMessage() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Test FCM Notification")
                }
            }

            // Content based on selected tab
            Box(
                modifier = Modifier.fillMaxSize(),
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
                                    text = "No events found",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(state.events) { event ->
                                    eventCard(
                                        event = event,
                                        onClick = { onNavigateToEventDetail(event.id) },
                                        viewModel = viewModel,
                                    )
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

    // Calendar permission dialog
    if (showPermissionDialog) {
        val permissionHandler = LocalPermissionHandler.current
        CalendarPermissionDialog(
            onDismiss = {
                showPermissionDialog = false
                permissionDialogDismissed = true
            },
            onRequestPermission = {
                showPermissionDialog = false
                permissionDialogDismissed = true
                // Request permissions through MainActivity
                permissionHandler.requestCalendarPermissions()
            },
            permissionHandler = permissionHandler,
        )
    }

    // Google account dialog
    if (showGoogleAccountDialog) {
        val permissionHandler = LocalPermissionHandler.current
        GoogleAccountDialog(
            onDismiss = {
                showGoogleAccountDialog = false
                googleAccountDialogDismissed = true
            },
            onOpenSettings = {
                showGoogleAccountDialog = false
                googleAccountDialogDismissed = true
                // Open device settings for account management
                permissionHandler.openAppSettings()
            },
        )
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
    val hasCalendarPermission = viewModel.hasCalendarPermission()
    val hasGoogleAccount = viewModel.hasGoogleAccountSetUp()
    val isInCalendar = viewModel.isEventInCalendar(event)

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

            // Calendar status for attending users
            if (isUserAttending && event.status == EventStatus.UPCOMING) {
                Spacer(modifier = Modifier.height(4.dp))
                when {
                    !hasCalendarPermission -> {
                        Text(
                            text = "📅 Calendar permission needed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    hasCalendarPermission && !hasGoogleAccount -> {
                        Text(
                            text = "📅 Google account needed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    hasCalendarPermission && hasGoogleAccount && isInCalendar -> {
                        Text(
                            text = "📅 Added to calendar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    hasCalendarPermission && hasGoogleAccount && !isInCalendar -> {
                        Text(
                            text = "📅 Not in calendar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

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
