package com.maegankullenda.carsonsunday.service

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.maegankullenda.carsonsunday.domain.repository.AuthRepository
import com.maegankullenda.carsonsunday.util.PushNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class CarsOnSundayMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationManager: PushNotificationManager

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "From: ${remoteMessage.from}")

        // Handle notification payload
        remoteMessage.data.let { data ->
            val eventId = data["eventId"]
            val notificationType = data["type"] // "new_event" or "event_updated"
            val title = data["title"] ?: "Cars On Sunday"
            val body = data["body"] ?: "New event notification"

            Log.d(TAG, "Message data payload: $data")

            notificationManager.showNotification(
                title = title,
                body = body,
                eventId = eventId,
                type = notificationType,
            )
        }

        // Handle notification payload if it exists
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Message Notification Body: ${notification.body}")
            notificationManager.showNotification(
                title = notification.title ?: "Cars On Sunday",
                body = notification.body ?: "New notification",
                eventId = null,
                type = null,
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")

        // Send token to server
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        // Store FCM token in Firestore for the current user
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                currentUser?.let { user ->
                    firestore.collection("users")
                        .document(user.id)
                        .update("fcmToken", token)
                        .await()
                    Log.d(TAG, "FCM token updated for user: ${user.username}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update FCM token", e)
            }
        }
    }

    companion object {
        private const val TAG = "CarsOnSundayFCM"
    }
}
