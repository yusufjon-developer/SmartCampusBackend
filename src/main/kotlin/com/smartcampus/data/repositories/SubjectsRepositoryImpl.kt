package com.smartcampus.data.repositories

import com.smartcampus.data.dao.SubjectsDao
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult
import com.smartcampus.domain.models.SubjectCreateRequest
import com.smartcampus.domain.models.SubjectDetailsDto
import com.smartcampus.domain.models.SubjectListItemDto
import com.smartcampus.domain.models.SubjectUpdateRequest
import com.smartcampus.domain.repositories.SubjectsRepository

class SubjectsRepositoryImpl(private val dao: SubjectsDao) : SubjectsRepository {
    override suspend fun getSubjects(params: PageRequestParams): PaginatedResult<SubjectListItemDto> =
        dao.getSubjects(params)

    override suspend fun getSubjectById(id: Int): SubjectDetailsDto? = dao.getSubjectById(id)
    override suspend fun createSubject(request: SubjectCreateRequest): SubjectDetailsDto =
        dao.createSubject(request.name)

    override suspend fun updateSubject(id: Int, request: SubjectUpdateRequest): SubjectDetailsDto? =
        dao.updateSubject(id, request.name)

    override suspend fun deleteSubject(id: Int): Boolean = dao.deleteSubject(id)
}
