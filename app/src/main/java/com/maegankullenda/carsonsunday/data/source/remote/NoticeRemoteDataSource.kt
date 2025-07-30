package com.maegankullenda.carsonsunday.data.source.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.maegankullenda.carsonsunday.domain.model.Notice
import com.maegankullenda.carsonsunday.domain.model.NoticePriority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoticeRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val noticesCollection = firestore.collection(COLLECTION_NOTICES)

    suspend fun saveNotice(notice: Notice): Result<Notice> {
        return try {
            val noticeData = mapOf(
                "id" to notice.id,
                "title" to notice.title,
                "content" to notice.content,
                "priority" to notice.priority.name,
                "createdBy" to notice.createdBy,
                "createdAt" to notice.createdAt.format(dateFormatter),
                "isActive" to notice.isActive,
            )
            
            noticesCollection.document(notice.id).set(noticeData).await()
            Result.success(notice)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNoticeById(noticeId: String): Notice? {
        return try {
            val document = noticesCollection.document(noticeId).get().await()
            if (document.exists()) {
                document.toNotice()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllNotices(): List<Notice> {
        return try {
            val query = noticesCollection.get().await()
            query.documents.mapNotNull { it.toNotice() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getNoticesByCreator(creatorId: String): List<Notice> {
        return try {
            val query = noticesCollection.whereEqualTo("createdBy", creatorId).get().await()
            query.documents.mapNotNull { it.toNotice() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateNotice(notice: Notice): Result<Notice> {
        return try {
            val noticeData = mapOf(
                "id" to notice.id,
                "title" to notice.title,
                "content" to notice.content,
                "priority" to notice.priority.name,
                "createdBy" to notice.createdBy,
                "createdAt" to notice.createdAt.format(dateFormatter),
                "isActive" to notice.isActive,
            )
            
            noticesCollection.document(notice.id).update(noticeData).await()
            Result.success(notice)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNotice(noticeId: String): Result<Unit> {
        return try {
            noticesCollection.document(noticeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeNotices(): Flow<List<Notice>> = callbackFlow {
        val listener = noticesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val notices = snapshot?.documents?.mapNotNull { it.toNotice() } ?: emptyList()
            trySend(notices)
        }
        
        awaitClose { listener.remove() }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toNotice(): Notice? {
        return try {
            val id = getString("id") ?: return null
            val title = getString("title") ?: return null
            val content = getString("content") ?: return null
            val priorityString = getString("priority") ?: return null
            val createdBy = getString("createdBy") ?: return null
            val createdAtString = getString("createdAt") ?: return null
            val isActive = getBoolean("isActive") ?: return null
            
            Notice(
                id = id,
                title = title,
                content = content,
                priority = NoticePriority.valueOf(priorityString),
                createdBy = createdBy,
                createdAt = LocalDateTime.parse(createdAtString, dateFormatter),
                isActive = isActive,
            )
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val COLLECTION_NOTICES = "notices"
        private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }
} 
