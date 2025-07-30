package com.maegankullenda.carsonsunday.data.repository.impl

import com.maegankullenda.carsonsunday.data.source.DataSourceManager
import com.maegankullenda.carsonsunday.domain.model.Notice
import com.maegankullenda.carsonsunday.domain.repository.NoticeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoticeRepositoryImpl @Inject constructor(
    private val dataSourceManager: DataSourceManager,
) : NoticeRepository {

    override suspend fun createNotice(notice: Notice): Result<Notice> {
        return try {
            val noticeDataSource = dataSourceManager.getNoticeDataSource()
            noticeDataSource.saveNotice(notice)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getNotices(): Flow<List<Notice>> {
        val noticeDataSource = dataSourceManager.getNoticeDataSource()
        return noticeDataSource.observeNotices()
    }

    override suspend fun getNoticeById(id: String): Notice? {
        val noticeDataSource = dataSourceManager.getNoticeDataSource()
        return noticeDataSource.getNoticeById(id)
    }

    override suspend fun updateNotice(notice: Notice): Result<Notice> {
        return try {
            val noticeDataSource = dataSourceManager.getNoticeDataSource()
            noticeDataSource.updateNotice(notice)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNotice(id: String): Result<Unit> {
        return try {
            val noticeDataSource = dataSourceManager.getNoticeDataSource()
            noticeDataSource.deleteNotice(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getNoticesByCreator(creatorId: String): Flow<List<Notice>> {
        val noticeDataSource = dataSourceManager.getNoticeDataSource()
        return noticeDataSource.observeNotices()
    }
}
