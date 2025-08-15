package com.maegankullenda.carsonsunday

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.maegankullenda.carsonsunday.domain.repository.AuthRepository
import com.maegankullenda.carsonsunday.ui.components.LocalPermissionHandler
import com.maegankullenda.carsonsunday.ui.components.PermissionHandler
import com.maegankullenda.carsonsunday.ui.navigation.appNavigation
import com.maegankullenda.carsonsunday.ui.theme.carsOnSundayTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), PermissionHandler {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var firestore: FirebaseFirestore

    private val calendarPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val calendarWriteGranted = permissions[Manifest.permission.WRITE_CALENDAR] == true
        val calendarReadGranted = permissions[Manifest.permission.READ_CALENDAR] == true
        if (calendarWriteGranted && calendarReadGranted) {
            println("Calendar permissions granted (read/write)")
        } else {
            println("Calendar permissions denied")
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
            setupFCM()
        } else {
            Log.d(TAG, "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Setup FCM and request notification permissions
        requestNotificationPermissionAndSetupFCM()
        
        setContent {
            carsOnSundayTheme {
                CompositionLocalProvider(LocalPermissionHandler provides this) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        val navController = rememberNavController()
                        
                        // Handle deep links from calendar
                        handleDeepLink(intent, navController)
                        
                        appNavigation(navController = navController)
                    }
                }
            }
        }
    }

    private fun handleDeepLink(intent: Intent, navController: androidx.navigation.NavController?) {
        val data: Uri? = intent.data
        if (data != null && data.scheme == "carsonsunday") {
            val path = data.path
            if (path != null && path.startsWith("/event_detail/")) {
                val eventId = path.substringAfter("/event_detail/")
                navController?.navigate("event_detail/$eventId")
            }
        }
    }

    override fun requestCalendarPermissions() {
        val hasWrite = checkSelfPermission(Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
        val hasRead = checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
        
        if (hasWrite && hasRead) {
            println("Calendar permissions already granted")
            return
        }

        // Request both permissions directly
        calendarPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission.READ_CALENDAR,
            ),
        )
    }

    override fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun requestNotificationPermissionAndSetupFCM() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                setupFCM()
            }
        } else {
            // For Android 12 and below, notification permission is granted by default
            setupFCM()
        }
    }

    private fun setupFCM() {
        // Get FCM token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d(TAG, "FCM Registration Token: $token")

            // Send token to server
            sendTokenToServer(token)
        }
    }

    private fun sendTokenToServer(token: String) {
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
        private const val TAG = "MainActivity"
    }
}
