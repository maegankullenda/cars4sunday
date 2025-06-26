package com.maegankullenda.carsonsunday.data.repository.impl

import com.maegankullenda.carsonsunday.data.source.local.NoticeLocalDataSource
import com.maegankullenda.carsonsunday.domain.model.Notice
import com.maegankullenda.carsonsunday.domain.repository.NoticeRepository
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoticeRepositoryImpl @Inject constructor(
    private val noticeLocalDataSource: NoticeLocalDataSource,
) : NoticeRepository {

    override suspend fun createNotice(notice: Notice): Result<Notice> {
        return try {
            noticeLocalDataSource.saveNotice(notice)
            Result.success(notice)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    override fun getNotices(): Flow<List<Notice>> {
        return noticeLocalDataSource.notices
    }

    override suspend fun getNoticeById(id: String): Notice? {
        return noticeLocalDataSource.getNoticeById(id)
    }

    override suspend fun updateNotice(notice: Notice): Result<Notice> {
        return try {
            noticeLocalDataSource.updateNotice(notice)
            Result.success(notice)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNotice(id: String): Result<Unit> {
        return try {
            noticeLocalDataSource.deleteNotice(id)
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    override fun getNoticesByCreator(creatorId: String): Flow<List<Notice>> {
        return noticeLocalDataSource.notices
    }
}
