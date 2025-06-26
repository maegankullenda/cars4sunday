@file:Suppress("ReturnCount")

package com.maegankullenda.carsonsunday.domain.usecase

import com.maegankullenda.carsonsunday.domain.model.User
import com.maegankullenda.carsonsunday.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        username: String,
        password: String,
        name: String,
        surname: String,
        mobileNumber: String,
    ): Result<User> {
        if (username.isBlank()) {
            return Result.failure(IllegalArgumentException("Username cannot be empty"))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Password cannot be empty"))
        }
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Name cannot be empty"))
        }
        if (surname.isBlank()) {
            return Result.failure(IllegalArgumentException("Surname cannot be empty"))
        }
        if (mobileNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("Mobile number cannot be empty"))
        }

        return authRepository.register(username, password, name, surname, mobileNumber)
    }
}
