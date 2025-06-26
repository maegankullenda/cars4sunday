package com.maegankullenda.carsonsunday.data.source.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.maegankullenda.carsonsunday.domain.model.Notice
import com.maegankullenda.carsonsunday.domain.model.NoticePriority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoticeLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )
    private val gson = Gson()

    private val _notices = MutableStateFlow<List<Notice>>(emptyList())
    val notices: Flow<List<Notice>> = _notices.asStateFlow()

    init {
        loadNotices()
    }

    fun saveNotice(notice: Notice) {
        val currentNotices = _notices.value.toMutableList()
        currentNotices.add(notice)
        _notices.value = currentNotices
        saveNoticesToStorage(currentNotices)
    }

    fun getNotices(): List<Notice> {
        return _notices.value
    }

    fun getNoticeById(id: String): Notice? {
        return _notices.value.find { it.id == id }
    }

    fun updateNotice(notice: Notice) {
        val currentNotices = _notices.value.toMutableList()
        val index = currentNotices.indexOfFirst { it.id == notice.id }
        if (index != -1) {
            currentNotices[index] = notice
            _notices.value = currentNotices
            saveNoticesToStorage(currentNotices)
        }
    }

    fun deleteNotice(id: String) {
        val currentNotices = _notices.value.toMutableList()
        currentNotices.removeAll { it.id == id }
        _notices.value = currentNotices
        saveNoticesToStorage(currentNotices)
    }

    fun getNoticesByCreator(creatorId: String): List<Notice> {
        return _notices.value.filter { it.createdBy == creatorId }
    }

    private fun loadNotices() {
        val noticesJson = prefs.getString(KEY_NOTICES, "[]")
        val noticesList: List<NoticeDto> = gson.fromJson(noticesJson, object : TypeToken<List<NoticeDto>>() {}.type)
        _notices.value = noticesList.map { it.toNotice() }
    }

    private fun saveNoticesToStorage(notices: List<Notice>) {
        val noticesDto = notices.map { NoticeDto.fromNotice(it) }
        val noticesJson = gson.toJson(noticesDto)
        prefs.edit().putString(KEY_NOTICES, noticesJson).apply()
    }

    companion object {
        private const val PREFS_NAME = "notices_prefs"
        private const val KEY_NOTICES = "notices"
        private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }

    private data class NoticeDto(
        val id: String,
        val title: String,
        val content: String,
        val priority: String,
        val createdBy: String,
        val createdAt: String,
    ) {
        fun toNotice(): Notice {
            return Notice(
                id = id,
                title = title,
                content = content,
                priority = NoticePriority.valueOf(priority),
                createdBy = createdBy,
                createdAt = LocalDateTime.parse(createdAt, dateFormatter),
            )
        }

        companion object {
            fun fromNotice(notice: Notice): NoticeDto {
                return NoticeDto(
                    id = notice.id,
                    title = notice.title,
                    content = notice.content,
                    priority = notice.priority.name,
                    createdBy = notice.createdBy,
                    createdAt = notice.createdAt.format(dateFormatter),
                )
            }
        }
    }
}
