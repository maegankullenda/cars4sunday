package com.maegankullenda.carsonsunday.domain.model

enum class UserRole {
    ADMIN,
    USER,
}

data class User(
    val id: String,
    val username: String,
    val password: String,
    val name: String,
    val surname: String,
    val mobileNumber: String,
    val role: UserRole = UserRole.USER,
)
