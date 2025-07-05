package com.maegankullenda.carsonsunday.domain.model

import java.time.LocalDateTime

enum class EventStatus {
    UPCOMING,
    COMPLETED,
    CANCELLED,
}

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val date: LocalDateTime,
    val location: String,
    val attendeeLimit: Int? = null, // null means no limit
    val attendees: List<String> = emptyList(), // List of user IDs who are attending
    val createdBy: String, // User ID
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true,
    val status: EventStatus = EventStatus.UPCOMING,
) {
    val attendeeCount: Int
        get() = attendees.size

    val isAtCapacity: Boolean
        get() = attendeeLimit != null && attendeeCount >= attendeeLimit

    fun canUserAttend(userId: String): Boolean {
        return status == EventStatus.UPCOMING &&
            !isAtCapacity &&
            !attendees.contains(userId)
    }
}
