@file:Suppress("ReturnCount")

package com.maegankullenda.carsonsunday.domain.usecase

import com.maegankullenda.carsonsunday.domain.model.Notice
import com.maegankullenda.carsonsunday.domain.model.NoticePriority
import com.maegankullenda.carsonsunday.domain.model.UserRole
import com.maegankullenda.carsonsunday.domain.repository.AuthRepository
import com.maegankullenda.carsonsunday.domain.repository.NoticeRepository
import java.util.UUID
import javax.inject.Inject

class CreateNoticeUseCase @Inject constructor(
    private val noticeRepository: NoticeRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        title: String,
        content: String,
        priority: NoticePriority,
    ): Result<Notice> {
        // Validate input
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Title cannot be empty"))
        }
        if (content.isBlank()) {
            return Result.failure(IllegalArgumentException("Content cannot be empty"))
        }

        // Check if current user is admin
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            return Result.failure(Exception("User not authenticated"))
        }
        if (currentUser.role != UserRole.ADMIN) {
            return Result.failure(Exception("Only administrators can create notices"))
        }

        val notice = Notice(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            priority = priority,
            createdBy = currentUser.id,
        )

        return noticeRepository.createNotice(notice)
    }
}
