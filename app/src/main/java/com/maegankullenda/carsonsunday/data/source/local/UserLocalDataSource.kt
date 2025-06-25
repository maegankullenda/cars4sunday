package com.maegankullenda.carsonsunday.data.source.local

import android.content.Context
import android.content.SharedPreferences
import com.maegankullenda.carsonsunday.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: Flow<Boolean> = _isUserLoggedIn.asStateFlow()

    fun saveUser(user: User) {
        prefs.edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USERNAME, user.username)
            .putString(KEY_PASSWORD, user.password)
            .putString(KEY_NAME, user.name)
            .putString(KEY_SURNAME, user.surname)
            .putString(KEY_MOBILE_NUMBER, user.mobileNumber)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()

        _isUserLoggedIn.value = true
    }

    fun getUser(): User? {
        val userId = prefs.getString(KEY_USER_ID, null)
        val username = prefs.getString(KEY_USERNAME, null)
        val password = prefs.getString(KEY_PASSWORD, null)
        val name = prefs.getString(KEY_NAME, null)
        val surname = prefs.getString(KEY_SURNAME, null)
        val mobileNumber = prefs.getString(KEY_MOBILE_NUMBER, null)

        if (userId == null || username == null || password == null || name == null || surname == null || mobileNumber == null) {
            return null
        }

        return User(
            id = userId,
            username = username,
            password = password,
            name = name,
            surname = surname,
            mobileNumber = mobileNumber,
        )
    }

    fun clearUser() {
        prefs.edit().clear().apply()
        _isUserLoggedIn.value = false
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    companion object {
        private const val PREFS_NAME = "user_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_NAME = "name"
        private const val KEY_SURNAME = "surname"
        private const val KEY_MOBILE_NUMBER = "mobile_number"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
}
