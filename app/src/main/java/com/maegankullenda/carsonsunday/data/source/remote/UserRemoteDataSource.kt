package com.maegankullenda.carsonsunday.data.source.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.maegankullenda.carsonsunday.domain.model.User
import com.maegankullenda.carsonsunday.domain.model.UserRole
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val usersCollection = firestore.collection(COLLECTION_USERS)

    suspend fun saveUser(user: User): Result<User> {
        return try {
            val userData = mapOf(
                "id" to user.id,
                "username" to user.username,
                "password" to user.password, // In production, this should be hashed
                "name" to user.name,
                "surname" to user.surname,
                "mobileNumber" to user.mobileNumber,
                "role" to user.role.name,
                "fcmToken" to user.fcmToken,
                "notificationsEnabled" to user.notificationsEnabled,
            )

            usersCollection.document(user.id).set(userData).await()
            Result.success(user)
        } catch (e: Exception) {
            println("DEBUG: UserRemoteDataSource.saveUser() - Firebase error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            val document = usersCollection.document(userId).get().await()
            if (document.exists()) {
                document.toUser()
            } else {
                null
            }
        } catch (e: Exception) {
            println("DEBUG: UserRemoteDataSource.getUserById() - Firebase error: ${e.message}")
            throw e // Re-throw to trigger fallback
        }
    }

    suspend fun getUserByUsername(username: String): User? {
        return try {
            println("DEBUG: UserRemoteDataSource.getUserByUsername() - searching for: $username")
            val query = usersCollection.whereEqualTo("username", username).get().await()
            val user = query.documents.firstOrNull()?.toUser()
            println("DEBUG: UserRemoteDataSource.getUserByUsername() - found user: $user")
            user
        } catch (e: Exception) {
            println("DEBUG: UserRemoteDataSource.getUserByUsername() - Firebase error: ${e.message}")
            throw e // Re-throw to trigger fallback
        }
    }

    suspend fun getAllUsers(): List<User> {
        return try {
            val query = usersCollection.get().await()
            query.documents.mapNotNull { it.toUser() }
        } catch (e: Exception) {
            println("DEBUG: UserRemoteDataSource.getAllUsers() - Firebase error: ${e.message}")
            throw e // Re-throw to trigger fallback
        }
    }

    suspend fun updateUser(user: User): Result<User> {
        return try {
            val userData = mapOf(
                "id" to user.id,
                "username" to user.username,
                "password" to user.password,
                "name" to user.name,
                "surname" to user.surname,
                "mobileNumber" to user.mobileNumber,
                "role" to user.role.name,
                "fcmToken" to user.fcmToken,
                "notificationsEnabled" to user.notificationsEnabled,
            )

            usersCollection.document(user.id).update(userData).await()
            Result.success(user)
        } catch (e: Exception) {
            println("DEBUG: UserRemoteDataSource.updateUser() - Firebase error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            usersCollection.document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: UserRemoteDataSource.deleteUser() - Firebase error: ${e.message}")
            Result.failure(e)
        }
    }

    fun observeUsers(): Flow<List<User>> = callbackFlow {
        val listener = usersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("DEBUG: UserRemoteDataSource.observeUsers() - Firebase error: ${error.message}")
                close(error)
                return@addSnapshotListener
            }

            val users = snapshot?.documents?.mapNotNull { it.toUser() } ?: emptyList()
            trySend(users)
        }

        awaitClose { listener.remove() }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toUser(): User? {
        return try {
            val id = getString("id") ?: return null
            val username = getString("username") ?: return null
            val password = getString("password") ?: return null
            val name = getString("name") ?: return null
            val surname = getString("surname") ?: return null
            val mobileNumber = getString("mobileNumber") ?: return null
            val roleString = getString("role") ?: return null
            val fcmToken = getString("fcmToken")
            val notificationsEnabled = getBoolean("notificationsEnabled") ?: true

            User(
                id = id,
                username = username,
                password = password,
                name = name,
                surname = surname,
                mobileNumber = mobileNumber,
                role = UserRole.valueOf(roleString),
                fcmToken = fcmToken,
                notificationsEnabled = notificationsEnabled,
            )
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val COLLECTION_USERS = "users"
    }
}
