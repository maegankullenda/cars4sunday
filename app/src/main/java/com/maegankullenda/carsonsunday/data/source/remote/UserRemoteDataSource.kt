package com.maegankullenda.carsonsunday.data.source.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
            )
            
            usersCollection.document(user.id).set(userData).await()
            Result.success(user)
        } catch (e: Exception) {
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
            null
        }
    }

    suspend fun getUserByUsername(username: String): User? {
        return try {
            val query = usersCollection.whereEqualTo("username", username).get().await()
            query.documents.firstOrNull()?.toUser()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllUsers(): List<User> {
        return try {
            val query = usersCollection.get().await()
            query.documents.mapNotNull { it.toUser() }
        } catch (e: Exception) {
            emptyList()
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
            )
            
            usersCollection.document(user.id).update(userData).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            usersCollection.document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeUsers(): Flow<List<User>> = callbackFlow {
        val listener = usersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
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
            
            User(
                id = id,
                username = username,
                password = password,
                name = name,
                surname = surname,
                mobileNumber = mobileNumber,
                role = UserRole.valueOf(roleString),
            )
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val COLLECTION_USERS = "users"
    }
} 
