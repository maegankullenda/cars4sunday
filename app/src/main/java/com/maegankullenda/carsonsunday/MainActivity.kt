package com.maegankullenda.carsonsunday

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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
import com.maegankullenda.carsonsunday.ui.components.LocalPermissionHandler
import com.maegankullenda.carsonsunday.ui.components.PermissionHandler
import com.maegankullenda.carsonsunday.ui.navigation.appNavigation
import com.maegankullenda.carsonsunday.ui.theme.carsOnSundayTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity(), PermissionHandler {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
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
}
