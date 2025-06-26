package com.maegankullenda.carsonsunday.domain.repository

import com.maegankullenda.carsonsunday.domain.model.Notice
import kotlinx.coroutines.flow.Flow

interface NoticeRepository {
    suspend fun createNotice(notice: Notice): Result<Notice>
    fun getNotices(): Flow<List<Notice>>
    suspend fun getNoticeById(id: String): Notice?
    suspend fun updateNotice(notice: Notice): Result<Notice>
    suspend fun deleteNotice(id: String): Result<Unit>
    fun getNoticesByCreator(creatorId: String): Flow<List<Notice>>
}
