package com.maegankullenda.carsonsunday.domain.model

import java.time.LocalDateTime

data class Notice(
    val id: String,
    val title: String,
    val content: String,
    val priority: NoticePriority,
    val createdBy: String, // User ID
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true,
)

enum class NoticePriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT,
}
