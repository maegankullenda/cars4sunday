package com.maegankullenda.carsonsunday.ui.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.maegankullenda.carsonsunday.domain.model.Event
import com.maegankullenda.carsonsunday.domain.model.EventStatus
import com.maegankullenda.carsonsunday.domain.model.UserRole
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun eventDetailScreen(
    eventId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEditEvent: (String) -> Unit,
    onNavigateToRespondents: (String) -> Unit,
    viewModel: EventsViewModel = hiltViewModel(),
) {
    var event by remember { mutableStateOf<Event?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAdmin = currentUser?.role == UserRole.ADMIN
    val dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)

    fun calculateDaysUntilEvent(eventDate: LocalDateTime): Long {
        val now = LocalDateTime.now()
        return if (eventDate.isAfter(now)) {
            java.time.Duration.between(now, eventDate).toDays()
        } else {
            0L
        }
    }

    LaunchedEffect(eventId, viewModel) {
        event = viewModel.getEventById(eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            event?.let { currentEvent ->
                Text(text = currentEvent.title, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Location: ${currentEvent.location}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Date: ${currentEvent.date.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (currentEvent.status == EventStatus.UPCOMING) {
                    val daysUntilEvent = calculateDaysUntilEvent(currentEvent.date)
                    val statusText = when {
                        daysUntilEvent == 0L -> "UPCOMING TODAY"
                        daysUntilEvent == 1L -> "UPCOMING TOMORROW"
                        else -> "UPCOMING in $daysUntilEvent days"
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Text(
                        text = "Status: ${currentEvent.status.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = when (currentEvent.status) {
                            EventStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
                            EventStatus.CANCELLED -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        },
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = currentEvent.description, style = MaterialTheme.typography.bodyLarge)

                // Admin actions - only show for upcoming events
                if (isAdmin && currentEvent.status == EventStatus.UPCOMING) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // View Respondents button
                        OutlinedButton(
                            onClick = { onNavigateToRespondents(currentEvent.id) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "View Respondents")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("View Respondents (${currentEvent.attendeeCount})")
                        }

                        // Edit and Cancel buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedButton(
                                onClick = { onNavigateToEditEvent(currentEvent.id) },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Edit Event")
                            }

                            OutlinedButton(
                                onClick = { showCancelDialog = true },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel")
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Cancel Event")
                            }
                        }
                    }
                }
            } ?: run {
                Text(
                    "Event not found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }

    // Cancel confirmation dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Event") },
            text = { Text("Are you sure you want to cancel this event? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        event?.let { currentEvent ->
                            viewModel.cancelEvent(currentEvent.id)
                            showCancelDialog = false
                            onNavigateBack()
                        }
                    },
                ) {
                    Text("Cancel Event")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Event")
                }
            },
        )
    }
}
