package com.smartcampus.features.subjects

import com.smartcampus.data.dao.SubjectsDao
import com.smartcampus.domain.models.SubjectCreateRequest
import com.smartcampus.domain.models.SubjectDto
import com.smartcampus.domain.models.SubjectUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult

class SubjectsService(private val dao: SubjectsDao) {

    suspend fun listSubjects(params: PageRequestParams): PaginatedResult<SubjectDto> {
        return dao.listSubjects(params)
    }

    suspend fun getSubject(id: Int): SubjectDto? {
        return dao.getSubjectById(id)
    }

    suspend fun createSubject(req: SubjectCreateRequest): SubjectDto {
        return dao.createSubject(req)
    }

    suspend fun updateSubject(id: Int, req: SubjectUpdateRequest): SubjectDto? {
        return dao.updateSubject(id, req)
    }

    suspend fun deleteSubject(id: Int): Boolean {
        return dao.deleteSubject(id)
    }
}
