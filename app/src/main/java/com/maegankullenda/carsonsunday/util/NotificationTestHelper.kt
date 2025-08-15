package com.maegankullenda.carsonsunday.util

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.maegankullenda.carsonsunday.domain.model.Event
import com.maegankullenda.carsonsunday.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Temporary helper class to test notifications manually before Cloud Functions are deployed.
 * This simulates what the Cloud Functions will do automatically.
 */
@Singleton
class NotificationTestHelper @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val pushNotificationManager: PushNotificationManager,
) {

    /**
     * Manually trigger a "new event" notification for testing.
     * This simulates what the Cloud Function will do when an event is created.
     */
    suspend fun testNewEventNotification(event: Event) {
        try {
            Log.d(TAG, "Testing new event notification for: ${event.title}")
            
            // Simulate the notification that would be sent by Cloud Functions
            pushNotificationManager.showNotification(
                title = "New Event Available!",
                body = "${event.title} - ${formatDate(event.date.toString())}",
                eventId = event.id,
                type = "new_event"
            )
            
            Log.d(TAG, "Test notification sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send test notification", e)
        }
    }

    /**
     * Manually trigger an "event updated" notification for testing.
     * This simulates what the Cloud Function will do when an event is updated.
     */
    suspend fun testEventUpdateNotification(event: Event, changeDescription: String = "has been updated") {
        try {
            Log.d(TAG, "Testing event update notification for: ${event.title}")
            
            // Check if current user is attending this event
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null && event.attendees.contains(currentUser.id)) {
                pushNotificationManager.showNotification(
                    title = "Event Updated",
                    body = "${event.title} $changeDescription",
                    eventId = event.id,
                    type = "event_updated"
                )
                
                Log.d(TAG, "Test update notification sent successfully")
            } else {
                Log.d(TAG, "Current user is not attending this event, no notification sent")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send test update notification", e)
        }
    }

    /**
     * Send a test FCM message to the current device.
     * This tests the FCM token and messaging service.
     */
    suspend fun sendTestFCMMessage() {
        try {
            // Get the current FCM token
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "Current FCM token: $token")
            
            // Show a local notification to simulate FCM
            pushNotificationManager.showNotification(
                title = "FCM Test",
                body = "This is a test notification to verify FCM setup",
                eventId = null,
                type = "test"
            )
            
            Log.d(TAG, "Test FCM message sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send test FCM message", e)
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            // Simple date formatting - you can improve this
            dateString.take(10) // Just take the date part
        } catch (e: Exception) {
            dateString
        }
    }

    companion object {
        private const val TAG = "NotificationTestHelper"
    }
}
