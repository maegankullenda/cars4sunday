package com.maegankullenda.carsonsunday.domain.model

import java.time.LocalDateTime

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val date: LocalDateTime,
    val location: String,
    val createdBy: String, // User ID
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true,
)
