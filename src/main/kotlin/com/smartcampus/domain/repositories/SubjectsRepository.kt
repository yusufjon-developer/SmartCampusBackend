package com.smartcampus.domain.repositories

import com.smartcampus.domain.models.SubjectCreateRequest
import com.smartcampus.domain.models.SubjectDetailsDto
import com.smartcampus.domain.models.SubjectListItemDto
import com.smartcampus.domain.models.SubjectUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult

interface SubjectsRepository {
    suspend fun getSubjects(params: PageRequestParams): PaginatedResult<SubjectListItemDto>
    suspend fun getSubjectById(id: Int): SubjectDetailsDto?
    suspend fun createSubject(request: SubjectCreateRequest): SubjectDetailsDto
    suspend fun updateSubject(id: Int, request: SubjectUpdateRequest): SubjectDetailsDto?
    suspend fun deleteSubject(id: Int): Boolean
}
