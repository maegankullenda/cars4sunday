package com.maegankullenda.carsonsunday.domain.usecase

import com.maegankullenda.carsonsunday.domain.model.Notice
import com.maegankullenda.carsonsunday.domain.repository.NoticeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNoticesUseCase @Inject constructor(
    private val noticeRepository: NoticeRepository,
) {
    operator fun invoke(): Flow<List<Notice>> {
        return noticeRepository.getNotices()
    }
}
