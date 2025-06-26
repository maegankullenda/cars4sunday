package com.maegankullenda.carsonsunday.domain.repository

import com.maegankullenda.carsonsunday.domain.model.User
import com.maegankullenda.carsonsunday.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
    suspend fun register(
        username: String,
        password: String,
        name: String,
        surname: String,
        mobileNumber: String,
        role: UserRole = UserRole.USER,
    ): Result<User>
    suspend fun getCurrentUser(): User?
    suspend fun logout()
    fun isUserLoggedIn(): Flow<Boolean>
    suspend fun makeUserAdmin(username: String): Result<User>
}
