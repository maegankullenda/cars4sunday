package com.maegankullenda.carsonsunday.data.repository.impl

import com.maegankullenda.carsonsunday.data.source.local.UserLocalDataSource
import com.maegankullenda.carsonsunday.domain.model.User
import com.maegankullenda.carsonsunday.domain.model.UserRole
import com.maegankullenda.carsonsunday.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val user = userLocalDataSource.getUserByUsername(username)
            if (user != null && user.password == password) {
                // Set the current user when login is successful
                userLocalDataSource.saveUser(user)
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid username or password"))
            }
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: IllegalArgumentException) {
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
            val existingUser = userLocalDataSource.getUserByUsername(username)
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

            userLocalDataSource.saveUser(newUser)
            Result.success(newUser)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): User? {
        return userLocalDataSource.getUser()
    }

    override suspend fun logout() {
        userLocalDataSource.clearUser()
    }

    override fun isUserLoggedIn(): Flow<Boolean> {
        return userLocalDataSource.isUserLoggedIn
    }

    override suspend fun makeUserAdmin(username: String): Result<User> {
        return try {
            val user = userLocalDataSource.getUserByUsername(username)
            if (user == null) {
                return Result.failure(Exception("User not found"))
            }

            val adminUser = user.copy(role = UserRole.ADMIN)
            userLocalDataSource.saveUser(adminUser)
            Result.success(adminUser)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    override suspend fun getAllUsers(): List<User> {
        return userLocalDataSource.getAllUsers()
    }

    override suspend fun getUserById(userId: String): User? {
        return userLocalDataSource.getUserById(userId)
    }
}
