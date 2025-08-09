package com.maegankullenda.carsonsunday.ui.components

import androidx.compose.runtime.compositionLocalOf

/**
 * Interface for handling permission requests
 */
interface PermissionHandler {
    fun requestCalendarPermissions()
    fun openAppSettings()
}

/**
 * CompositionLocal to provide PermissionHandler to Compose UI
 */
val LocalPermissionHandler = compositionLocalOf<PermissionHandler> { 
    error("PermissionHandler not provided") 
} 