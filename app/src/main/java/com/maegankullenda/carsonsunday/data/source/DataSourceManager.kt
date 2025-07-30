package com.maegankullenda.carsonsunday.data.source

import com.maegankullenda.carsonsunday.data.source.local.EventLocalDataSource
import com.maegankullenda.carsonsunday.data.source.local.NoticeLocalDataSource
import com.maegankullenda.carsonsunday.data.source.local.UserLocalDataSource
import com.maegankullenda.carsonsunday.data.source.remote.EventRemoteDataSource
import com.maegankullenda.carsonsunday.data.source.remote.NoticeRemoteDataSource
import com.maegankullenda.carsonsunday.data.source.remote.UserRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSourceManager @Inject constructor(
    val userLocalDataSource: UserLocalDataSource,
    val eventLocalDataSource: EventLocalDataSource,
    val noticeLocalDataSource: NoticeLocalDataSource,
    val userRemoteDataSource: UserRemoteDataSource,
    val eventRemoteDataSource: EventRemoteDataSource,
    val noticeRemoteDataSource: NoticeRemoteDataSource,
) {
    private var useRemoteStorage = true // Default to Firebase remote storage

    fun setUseRemoteStorage(useRemote: Boolean) {
        useRemoteStorage = useRemote
    }

    fun isUsingRemoteStorage(): Boolean = useRemoteStorage

    // User data source
    fun getUserDataSource(): UserDataSource {
        return if (useRemoteStorage) {
            object : UserDataSource {
                override suspend fun saveUser(user: com.maegankullenda.carsonsunday.domain.model.User): Result<com.maegankullenda.carsonsunday.domain.model.User> {
                    return userRemoteDataSource.saveUser(user)
                }

                override suspend fun getUserById(userId: String): com.maegankullenda.carsonsunday.domain.model.User? {
                    return userRemoteDataSource.getUserById(userId)
                }

                override suspend fun getUserByUsername(username: String): com.maegankullenda.carsonsunday.domain.model.User? {
                    return userRemoteDataSource.getUserByUsername(username)
                }

                override suspend fun getAllUsers(): List<com.maegankullenda.carsonsunday.domain.model.User> {
                    return userRemoteDataSource.getAllUsers()
                }

                override suspend fun updateUser(user: com.maegankullenda.carsonsunday.domain.model.User): Result<com.maegankullenda.carsonsunday.domain.model.User> {
                    return userRemoteDataSource.updateUser(user)
                }

                override suspend fun deleteUser(userId: String): Result<Unit> {
                    return userRemoteDataSource.deleteUser(userId)
                }

                override fun observeUsers(): kotlinx.coroutines.flow.Flow<List<com.maegankullenda.carsonsunday.domain.model.User>> {
                    return userRemoteDataSource.observeUsers()
                }
            }
        } else {
            object : UserDataSource {
                override suspend fun saveUser(user: com.maegankullenda.carsonsunday.domain.model.User): Result<com.maegankullenda.carsonsunday.domain.model.User> {
                    userLocalDataSource.saveUser(user)
                    return Result.success(user)
                }

                override suspend fun getUserById(userId: String): com.maegankullenda.carsonsunday.domain.model.User? {
                    return userLocalDataSource.getUserById(userId)
                }

                override suspend fun getUserByUsername(username: String): com.maegankullenda.carsonsunday.domain.model.User? {
                    return userLocalDataSource.getUserByUsername(username)
                }

                override suspend fun getAllUsers(): List<com.maegankullenda.carsonsunday.domain.model.User> {
                    return userLocalDataSource.getAllUsers()
                }

                override suspend fun updateUser(user: com.maegankullenda.carsonsunday.domain.model.User): Result<com.maegankullenda.carsonsunday.domain.model.User> {
                    userLocalDataSource.saveUser(user)
                    return Result.success(user)
                }

                override suspend fun deleteUser(userId: String): Result<Unit> {
                    // Local storage doesn't support delete, just return success
                    return Result.success(Unit)
                }

                override fun observeUsers(): kotlinx.coroutines.flow.Flow<List<com.maegankullenda.carsonsunday.domain.model.User>> {
                    return kotlinx.coroutines.flow.flow {
                        emit(userLocalDataSource.getAllUsers())
                    }
                }
            }
        }
    }

    // Event data source
    fun getEventDataSource(): EventDataSource {
        return if (useRemoteStorage) {
            object : EventDataSource {
                override suspend fun saveEvent(event: com.maegankullenda.carsonsunday.domain.model.Event): Result<com.maegankullenda.carsonsunday.domain.model.Event> {
                    return eventRemoteDataSource.saveEvent(event)
                }

                override suspend fun getEventById(eventId: String): com.maegankullenda.carsonsunday.domain.model.Event? {
                    return eventRemoteDataSource.getEventById(eventId)
                }

                override suspend fun getAllEvents(): List<com.maegankullenda.carsonsunday.domain.model.Event> {
                    return eventRemoteDataSource.getAllEvents()
                }

                override suspend fun getEventsByCreator(creatorId: String): List<com.maegankullenda.carsonsunday.domain.model.Event> {
                    return eventRemoteDataSource.getEventsByCreator(creatorId)
                }

                override suspend fun getEventsByStatus(status: com.maegankullenda.carsonsunday.domain.model.EventStatus): List<com.maegankullenda.carsonsunday.domain.model.Event> {
                    return eventRemoteDataSource.getEventsByStatus(status)
                }

                override suspend fun updateEvent(event: com.maegankullenda.carsonsunday.domain.model.Event): Result<com.maegankullenda.carsonsunday.domain.model.Event> {
                    return eventRemoteDataSource.updateEvent(event)
                }

                override suspend fun deleteEvent(eventId: String): Result<Unit> {
                    return eventRemoteDataSource.deleteEvent(eventId)
                }

                override suspend fun attendEvent(eventId: String, userId: String): Result<com.maegankullenda.carsonsunday.domain.model.Event> {
                    return eventRemoteDataSource.attendEvent(eventId, userId)
                }

                override suspend fun leaveEvent(eventId: String, userId: String): Result<com.maegankullenda.carsonsunday.domain.model.Event> {
                    return eventRemoteDataSource.leaveEvent(eventId, userId)
                }

                override suspend fun isUserAttending(eventId: String, userId: String): Boolean {
                    return eventRemoteDataSource.isUserAttending(eventId, userId)
                }

                override fun observeEvents(): kotlinx.coroutines.flow.Flow<List<com.maegankullenda.carsonsunday.domain.model.Event>> {
                    return eventRemoteDataSource.observeEvents()
                }

                override fun observeEventsByStatus(status: com.maegankullenda.carsonsunday.domain.model.EventStatus): kotlinx.coroutines.flow.Flow<List<com.maegankullenda.carsonsunday.domain.model.Event>> {
                    return eventRemoteDataSource.observeEventsByStatus(status)
                }
            }
        } else {
            object : EventDataSource {
                override suspend fun saveEvent(event: com.maegankullenda.carsonsunday.domain.model.Event): Result<com.maegankullenda.carsonsunday.domain.model.Event> {
                    eventLocalDataSource.saveEvent(event)
                    return Result.success(event)
                }

                override suspend fun getEventById(eventId: String): com.maegankullenda.carsonsunday.domain.model.Event? {
                    return eventLocalDataSource.getEventById(eventId)
                }

                override suspend fun getAllEvents(): List<com.maegankullenda.carsonsunday.domain.model.Event> {
                    return eventLocalDataSource.getEvents()
                }

                override suspend fun getEventsByCreator(creatorId: String): List<com.maegankullenda.carsonsunday.domain.model.Event> {
                    return eventLocalDataSource.getEventsByCreator(creatorId)
                }

                override suspend fun getEventsByStatus(status: com.maegankullenda.carsonsunday.domain.model.EventStatus): List<com.maegankullenda.carsonsunday.domain.model.Event> {
                    return eventLocalDataSource.getEventsByStatus(status)
                }

                override suspend fun updateEvent(event: com.maegankullenda.carsonsunday.domain.model.Event): Result<com.maegankullenda.carsonsunday.domain.model.Event> {
                    eventLocalDataSource.updateEvent(event)
                    return Result.success(event)
                }

                override suspend fun deleteEvent(eventId: String): Result<Unit> {
                    eventLocalDataSource.deleteEvent(eventId)
                    return Result.success(Unit)
                }

                override suspend fun attendEvent(eventId: String, userId: String): Result<com.maegankullenda.carsonsunday.domain.model.Event> {
                    val event = eventLocalDataSource.getEventById(eventId)
                    if (event == null) {
                        return Result.failure(Exception("Event not found"))
                    }
                    val updatedEvent = event.copy(attendees = event.attendees + userId)
                    eventLocalDataSource.updateEvent(updatedEvent)
                    return Result.success(updatedEvent)
                }

                override suspend fun leaveEvent(eventId: String, userId: String): Result<com.maegankullenda.carsonsunday.domain.model.Event> {
                    val event = eventLocalDataSource.getEventById(eventId)
                    if (event == null) {
                        return Result.failure(Exception("Event not found"))
                    }
                    val updatedEvent = event.copy(attendees = event.attendees - userId)
                    eventLocalDataSource.updateEvent(updatedEvent)
                    return Result.success(updatedEvent)
                }

                override suspend fun isUserAttending(eventId: String, userId: String): Boolean {
                    val event = eventLocalDataSource.getEventById(eventId)
                    return event?.attendees?.contains(userId) == true
                }

                override fun observeEvents(): kotlinx.coroutines.flow.Flow<List<com.maegankullenda.carsonsunday.domain.model.Event>> {
                    return eventLocalDataSource.events
                }

                override fun observeEventsByStatus(status: com.maegankullenda.carsonsunday.domain.model.EventStatus): kotlinx.coroutines.flow.Flow<List<com.maegankullenda.carsonsunday.domain.model.Event>> {
                    return kotlinx.coroutines.flow.flow {
                        emit(eventLocalDataSource.getEventsByStatus(status))
                    }
                }
            }
        }
    }

    // Notice data source
    fun getNoticeDataSource(): NoticeDataSource {
        return if (useRemoteStorage) {
            object : NoticeDataSource {
                override suspend fun saveNotice(notice: com.maegankullenda.carsonsunday.domain.model.Notice): Result<com.maegankullenda.carsonsunday.domain.model.Notice> {
                    return noticeRemoteDataSource.saveNotice(notice)
                }

                override suspend fun getNoticeById(noticeId: String): com.maegankullenda.carsonsunday.domain.model.Notice? {
                    return noticeRemoteDataSource.getNoticeById(noticeId)
                }

                override suspend fun getAllNotices(): List<com.maegankullenda.carsonsunday.domain.model.Notice> {
                    return noticeRemoteDataSource.getAllNotices()
                }

                override suspend fun getNoticesByCreator(creatorId: String): List<com.maegankullenda.carsonsunday.domain.model.Notice> {
                    return noticeRemoteDataSource.getNoticesByCreator(creatorId)
                }

                override suspend fun updateNotice(notice: com.maegankullenda.carsonsunday.domain.model.Notice): Result<com.maegankullenda.carsonsunday.domain.model.Notice> {
                    return noticeRemoteDataSource.updateNotice(notice)
                }

                override suspend fun deleteNotice(noticeId: String): Result<Unit> {
                    return noticeRemoteDataSource.deleteNotice(noticeId)
                }

                override fun observeNotices(): kotlinx.coroutines.flow.Flow<List<com.maegankullenda.carsonsunday.domain.model.Notice>> {
                    return noticeRemoteDataSource.observeNotices()
                }
            }
        } else {
            object : NoticeDataSource {
                override suspend fun saveNotice(notice: com.maegankullenda.carsonsunday.domain.model.Notice): Result<com.maegankullenda.carsonsunday.domain.model.Notice> {
                    noticeLocalDataSource.saveNotice(notice)
                    return Result.success(notice)
                }

                override suspend fun getNoticeById(noticeId: String): com.maegankullenda.carsonsunday.domain.model.Notice? {
                    return noticeLocalDataSource.getNoticeById(noticeId)
                }

                override suspend fun getAllNotices(): List<com.maegankullenda.carsonsunday.domain.model.Notice> {
                    return noticeLocalDataSource.getNotices()
                }

                override suspend fun getNoticesByCreator(creatorId: String): List<com.maegankullenda.carsonsunday.domain.model.Notice> {
                    return noticeLocalDataSource.getNoticesByCreator(creatorId)
                }

                override suspend fun updateNotice(notice: com.maegankullenda.carsonsunday.domain.model.Notice): Result<com.maegankullenda.carsonsunday.domain.model.Notice> {
                    noticeLocalDataSource.updateNotice(notice)
                    return Result.success(notice)
                }

                override suspend fun deleteNotice(noticeId: String): Result<Unit> {
                    noticeLocalDataSource.deleteNotice(noticeId)
                    return Result.success(Unit)
                }

                override fun observeNotices(): kotlinx.coroutines.flow.Flow<List<com.maegankullenda.carsonsunday.domain.model.Notice>> {
                    return noticeLocalDataSource.notices
                }
            }
        }
    }
}

interface UserDataSource {
    suspend fun saveUser(user: com.maegankullenda.carsonsunday.domain.model.User): Result<com.maegankullenda.carsonsunday.domain.model.User>
    suspend fun getUserById(userId: String): com.maegankullenda.carsonsunday.domain.model.User?
    suspend fun getUserByUsername(username: String): com.maegankullenda.carsonsunday.domain.model.User?
    suspend fun getAllUsers(): List<com.maegankullenda.carsonsunday.domain.model.User>
    suspend fun updateUser(user: com.maegankullenda.carsonsunday.domain.model.User): Result<com.maegankullenda.carsonsunday.domain.model.User>
    suspend fun deleteUser(userId: String): Result<Unit>
    fun observeUsers(): kotlinx.coroutines.flow.Flow<List<com.maegankullenda.carsonsunday.domain.model.User>>
}

interface EventDataSource {
    suspend fun saveEvent(event: com.maegankullenda.carsonsunday.domain.model.Event): Result<com.maegankullenda.carsonsunday.domain.model.Event>
    suspend fun getEventById(eventId: String): com.maegankullenda.carsonsunday.domain.model.Event?
    suspend fun getAllEvents(): List<com.maegankullenda.carsonsunday.domain.model.Event>
    suspend fun getEventsByCreator(creatorId: String): List<com.maegankullenda.carsonsunday.domain.model.Event>
    suspend fun getEventsByStatus(status: com.maegankullenda.carsonsunday.domain.model.EventStatus): List<com.maegankullenda.carsonsunday.domain.model.Event>
    suspend fun updateEvent(event: com.maegankullenda.carsonsunday.domain.model.Event): Result<com.maegankullenda.carsonsunday.domain.model.Event>
    suspend fun deleteEvent(eventId: String): Result<Unit>
    suspend fun attendEvent(eventId: String, userId: String): Result<com.maegankullenda.carsonsunday.domain.model.Event>
    suspend fun leaveEvent(eventId: String, userId: String): Result<com.maegankullenda.carsonsunday.domain.model.Event>
    suspend fun isUserAttending(eventId: String, userId: String): Boolean
    fun observeEvents(): kotlinx.coroutines.flow.Flow<List<com.maegankullenda.carsonsunday.domain.model.Event>>
    fun observeEventsByStatus(status: com.maegankullenda.carsonsunday.domain.model.EventStatus): kotlinx.coroutines.flow.Flow<List<com.maegankullenda.carsonsunday.domain.model.Event>>
}

interface NoticeDataSource {
    suspend fun saveNotice(notice: com.maegankullenda.carsonsunday.domain.model.Notice): Result<com.maegankullenda.carsonsunday.domain.model.Notice>
    suspend fun getNoticeById(noticeId: String): com.maegankullenda.carsonsunday.domain.model.Notice?
    suspend fun getAllNotices(): List<com.maegankullenda.carsonsunday.domain.model.Notice>
    suspend fun getNoticesByCreator(creatorId: String): List<com.maegankullenda.carsonsunday.domain.model.Notice>
    suspend fun updateNotice(notice: com.maegankullenda.carsonsunday.domain.model.Notice): Result<com.maegankullenda.carsonsunday.domain.model.Notice>
    suspend fun deleteNotice(noticeId: String): Result<Unit>
    fun observeNotices(): kotlinx.coroutines.flow.Flow<List<com.maegankullenda.carsonsunday.domain.model.Notice>>
} 
