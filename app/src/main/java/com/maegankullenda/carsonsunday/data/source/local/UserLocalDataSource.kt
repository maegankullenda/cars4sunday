package com.maegankullenda.carsonsunday.data.source.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
    private val gson = Gson()

    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: Flow<Boolean> = _isUserLoggedIn.asStateFlow()

    fun saveUser(user: User) {
        val users = getUsers().toMutableList()
        // Check if user already exists and update, otherwise add new user
        val existingUserIndex = users.indexOfFirst { it.username == user.username }
        if (existingUserIndex != -1) {
            users[existingUserIndex] = user
        } else {
            users.add(user)
        }
        saveUsers(users)
        // Set current logged in user
        setCurrentUser(user)
    }

    fun getUser(): User? {
        val currentUserId = prefs.getString(KEY_CURRENT_USER_ID, null) ?: return null
        return getUsers().find { it.id == currentUserId }
    }

    fun getUserByUsername(username: String): User? {
        return getUsers().find { it.username == username }
    }

    fun getUserById(userId: String): User? {
        return getUsers().find { it.id == userId }
    }

    fun getAllUsers(): List<User> {
        return getUsers()
    }

    private fun getUsers(): List<User> {
        val usersJson = prefs.getString(KEY_USERS, "[]")
        val type = object : TypeToken<List<User>>() {}.type
        return try {
            gson.fromJson(usersJson, type) ?: emptyList()
        } catch (e: com.google.gson.JsonSyntaxException) {
            Log.w("UserLocalDataSource", "Invalid JSON format for users, returning empty list", e)
            emptyList()
        } catch (e: com.google.gson.JsonParseException) {
            Log.w("UserLocalDataSource", "JSON parsing error for users, returning empty list", e)
            emptyList()
        }
    }

    private fun saveUsers(users: List<User>) {
        val usersJson = gson.toJson(users)
        prefs.edit().putString(KEY_USERS, usersJson).apply()
    }

    private fun setCurrentUser(user: User) {
        prefs.edit()
            .putString(KEY_CURRENT_USER_ID, user.id)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
        _isUserLoggedIn.value = true
    }

    fun clearUser() {
        prefs.edit()
            .remove(KEY_CURRENT_USER_ID)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
        _isUserLoggedIn.value = false
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    companion object {
        private const val PREFS_NAME = "user_prefs"
        private const val KEY_USERS = "users"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
}
