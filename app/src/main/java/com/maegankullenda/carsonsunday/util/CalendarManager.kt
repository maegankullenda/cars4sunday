package com.maegankullenda.carsonsunday.util

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.maegankullenda.carsonsunday.domain.model.Event
import java.time.ZoneId
import java.util.TimeZone
import javax.inject.Inject

class CalendarManager @Inject constructor(
    private val context: Context,
) {

    companion object {
        private const val APP_SCHEME = "carsonsunday"
        private const val EVENT_DETAIL_PATH = "event_detail"
    }

    fun hasCalendarPermission(): Boolean {
        val hasWrite = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
        val hasRead = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
        return hasWrite && hasRead
    }

    fun hasGoogleAccountSetUp(): Boolean {
        return try {
            if (!hasCalendarPermission()) return false
            val contentResolver: ContentResolver = context.contentResolver
            val cursor = contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                arrayOf(CalendarContract.Calendars._ID),
                "${CalendarContract.Calendars.VISIBLE} = 1 AND ${CalendarContract.Calendars.SYNC_EVENTS} = 1",
                null,
                null,
            )
            val hasAnyCalendar = cursor?.count ?: 0 > 0
            cursor?.close()
            hasAnyCalendar
        } catch (e: Exception) {
            false
        }
    }

    fun getAvailableCalendars(): List<CalendarAccount> {
        return try {
            if (!hasCalendarPermission()) return emptyList()
            val contentResolver: ContentResolver = context.contentResolver
            val cursor = contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                arrayOf(
                    CalendarContract.Calendars._ID,
                    CalendarContract.Calendars.ACCOUNT_NAME,
                    CalendarContract.Calendars.ACCOUNT_TYPE,
                    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                    CalendarContract.Calendars.VISIBLE,
                    CalendarContract.Calendars.SYNC_EVENTS,
                    CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                ),
                "${CalendarContract.Calendars.VISIBLE} = 1 AND ${CalendarContract.Calendars.SYNC_EVENTS} = 1",
                null,
                null,
            )

            val calendars = mutableListOf<CalendarAccount>()
            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getLong(0)
                    val accountName = it.getString(1)
                    val accountType = it.getString(2)
                    val displayName = it.getString(3)
                    val visible = it.getInt(4) == 1
                    val sync = it.getInt(5) == 1
                    val accessLevel = it.getInt(6)

                    if (visible && sync) {
                        calendars.add(
                            CalendarAccount(
                                id = id,
                                accountName = accountName,
                                accountType = accountType,
                                displayName = displayName,
                                accessLevel = accessLevel,
                            ),
                        )
                    }
                }
            }
            calendars
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addEventToCalendar(event: Event): Result<Long> {
        return try {
            if (!hasCalendarPermission()) {
                return Result.failure(Exception("Calendar permission not granted"))
            }

            if (!hasGoogleAccountSetUp()) {
                return Result.failure(Exception("No calendar account available. Please ensure a Google calendar is added and sync is enabled on the device."))
            }

            val contentResolver: ContentResolver = context.contentResolver

            val calendarId = getDefaultCalendarId(contentResolver)

            val values = ContentValues().apply {
                put(CalendarContract.Events.TITLE, event.title)
                put(CalendarContract.Events.DESCRIPTION, buildEventDescription(event))
                put(CalendarContract.Events.EVENT_LOCATION, event.location)

                val startTime = event.date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endTime = event.date.plusHours(2).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                put(CalendarContract.Events.DTSTART, startTime)
                put(CalendarContract.Events.DTEND, endTime)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                // Do not set a non-standard 'url' column; include deep link in description instead
                put(CalendarContract.Events.HAS_ALARM, 1)
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
            }

            val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = uri?.lastPathSegment?.toLongOrNull()

            if (eventId != null) {
                addEventReminder(eventId, 15)
                Result.success(eventId)
            } else {
                Result.failure(Exception("Failed to add event to calendar"))
            }
        } catch (e: SecurityException) {
            Result.failure(Exception("Calendar permission denied at runtime: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun removeEventFromCalendar(event: Event): Result<Unit> {
        return try {
            if (!hasCalendarPermission()) {
                return Result.failure(Exception("Calendar permission not granted"))
            }

            val contentResolver: ContentResolver = context.contentResolver
            val selection = "${CalendarContract.Events.TITLE} = ? AND ${CalendarContract.Events.DESCRIPTION} LIKE ?"
            val selectionArgs = arrayOf(
                event.title,
                "%${event.id}%",
            )

            val deletedRows = contentResolver.delete(
                CalendarContract.Events.CONTENT_URI,
                selection,
                selectionArgs,
            )

            if (deletedRows > 0) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Event not found in calendar"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isEventInCalendar(event: Event): Boolean {
        return try {
            if (!hasCalendarPermission()) {
                return false
            }

            val contentResolver: ContentResolver = context.contentResolver
            val selection = "${CalendarContract.Events.TITLE} = ? AND ${CalendarContract.Events.DESCRIPTION} LIKE ?"
            val selectionArgs = arrayOf(
                event.title,
                "%${event.id}%",
            )

            val cursor = contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                arrayOf(CalendarContract.Events._ID),
                selection,
                selectionArgs,
                null,
            )

            val exists = cursor?.count ?: 0 > 0
            cursor?.close()
            exists
        } catch (e: Exception) {
            false
        }
    }

    private fun getDefaultCalendarId(contentResolver: ContentResolver): Long {
        // Prefer primary Google calendar with writable access (EDITOR or OWNER)
        var cursor = contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.ACCOUNT_TYPE,
                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                CalendarContract.Calendars.IS_PRIMARY,
            ),
            "${CalendarContract.Calendars.VISIBLE} = 1 AND ${CalendarContract.Calendars.SYNC_EVENTS} = 1 AND ${CalendarContract.Calendars.ACCOUNT_TYPE} = ? AND ${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ?",
            arrayOf("com.google", CalendarContract.Calendars.CAL_ACCESS_EDITOR.toString()),
            "${CalendarContract.Calendars.IS_PRIMARY} DESC",
        )
        cursor?.use { c ->
            if (c.moveToFirst()) {
                return c.getLong(0)
            }
        }

        // Fallback: any visible/syncable calendar with writable access
        cursor = contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
            ),
            "${CalendarContract.Calendars.VISIBLE} = 1 AND ${CalendarContract.Calendars.SYNC_EVENTS} = 1 AND ${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ?",
            arrayOf(CalendarContract.Calendars.CAL_ACCESS_EDITOR.toString()),
            "${CalendarContract.Calendars._ID} ASC",
        )
        cursor?.use { c ->
            if (c.moveToFirst()) {
                return c.getLong(0)
            }
        }

        // Last resort fallback
        return 1L
    }

    private fun addEventReminder(eventId: Long, minutesBefore: Int) {
        try {
            val contentResolver: ContentResolver = context.contentResolver
            val reminderValues = ContentValues().apply {
                put(CalendarContract.Reminders.EVENT_ID, eventId)
                put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                put(CalendarContract.Reminders.MINUTES, minutesBefore)
            }

            contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
        } catch (e: Exception) {
            // Keep minimal failure logging
        }
    }

    private fun buildEventDescription(event: Event): String {
        return buildString {
            appendLine(event.description)
            appendLine()
            appendLine("---")
            appendLine("Event ID: ${event.id}")
            appendLine("Created by: ${event.createdBy}")
            appendLine("Status: ${event.status}")
            appendLine()
            appendLine("View in CarsOnSunday app: ${buildEventDeepLink(event.id)}")
        }
    }

    private fun buildEventDeepLink(eventId: String): String {
        return "$APP_SCHEME://$EVENT_DETAIL_PATH/$eventId"
    }
}

/** Data class representing a calendar account */
data class CalendarAccount(
    val id: Long,
    val accountName: String,
    val accountType: String,
    val displayName: String,
    val accessLevel: Int,
) 