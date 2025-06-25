package com.maegankullenda.carsonsunday.data.repository.impl

import com.maegankullenda.carsonsunday.data.source.local.UserLocalDataSource
import com.maegankullenda.carsonsunday.domain.model.User
import com.maegankullenda.carsonsunday.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val user = userLocalDataSource.getUser()
            if (user != null && user.username == username && user.password == password) {
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
    ): Result<User> {
        return try {
            val existingUser = userLocalDataSource.getUser()
            if (existingUser != null && existingUser.username == username) {
                return Result.failure(Exception("Username already exists"))
            }

            val newUser = User(
                id = UUID.randomUUID().toString(),
                username = username,
                password = password,
                name = name,
                surname = surname,
                mobileNumber = mobileNumber,
            )

            userLocalDataSource.saveUser(newUser)
            Result.success(newUser)
        } catch (e: Exception) {
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
}
