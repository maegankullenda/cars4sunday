package com.maegankullenda.carsonsunday.data.repository.impl

import com.maegankullenda.carsonsunday.data.source.DataSourceManager
import com.maegankullenda.carsonsunday.domain.model.User
import com.maegankullenda.carsonsunday.domain.model.UserRole
import com.maegankullenda.carsonsunday.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val dataSourceManager: DataSourceManager,
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val userDataSource = dataSourceManager.getUserDataSource()
            val user = userDataSource.getUserByUsername(username)
            if (user != null && user.password == password) {
                // Set the current user in local storage for session management
                dataSourceManager.userLocalDataSource.saveUser(user)
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid username or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        username: String,
        password: String,
        name: String,
        surname: String,
        mobileNumber: String,
        role: UserRole,
    ): Result<User> {
        return try {
            val userDataSource = dataSourceManager.getUserDataSource()
            val existingUser = userDataSource.getUserByUsername(username)
            if (existingUser != null) {
                return Result.failure(Exception("Username already exists"))
            }

            val newUser = User(
                id = UUID.randomUUID().toString(),
                username = username,
                password = password,
                name = name,
                surname = surname,
                mobileNumber = mobileNumber,
                role = role,
            )

            userDataSource.saveUser(newUser)
            // Also save to local storage for session management
            dataSourceManager.userLocalDataSource.saveUser(newUser)
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): User? {
        val currentUserId = getCurrentUserId()
        if (currentUserId == null) return null
        val userDataSource = dataSourceManager.getUserDataSource()
        return userDataSource.getUserById(currentUserId)
    }

    override suspend fun logout() {
        // Clear current user from local storage (always use local for session management)
        dataSourceManager.userLocalDataSource.clearUser()
    }

    override fun isUserLoggedIn(): Flow<Boolean> {
        return dataSourceManager.userLocalDataSource.isUserLoggedIn
    }

    override suspend fun makeUserAdmin(username: String): Result<User> {
        return try {
            val userDataSource = dataSourceManager.getUserDataSource()
            val user = userDataSource.getUserByUsername(username)
            if (user == null) {
                return Result.failure(Exception("User not found"))
            }

            val adminUser = user.copy(role = UserRole.ADMIN)
            userDataSource.updateUser(adminUser)
            Result.success(adminUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllUsers(): List<User> {
        val userDataSource = dataSourceManager.getUserDataSource()
        return userDataSource.getAllUsers()
    }

    override suspend fun getUserById(userId: String): User? {
        val userDataSource = dataSourceManager.getUserDataSource()
        return userDataSource.getUserById(userId)
    }

    private fun getCurrentUserId(): String? {
        return dataSourceManager.userLocalDataSource.getUser()?.id
    }
}
