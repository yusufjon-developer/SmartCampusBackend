package com.smartcampus.data.repositories

import com.smartcampus.data.dao.GroupsDao
import com.smartcampus.domain.models.GroupCreateRequest
import com.smartcampus.domain.models.GroupDto
import com.smartcampus.domain.models.GroupUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult

class GroupsRepositoryImpl(private val dao: GroupsDao) {
    suspend fun listGroups(params: PageRequestParams): PaginatedResult<GroupDto> = dao.listGroups(params)
    suspend fun getGroupById(id: Int): GroupDto? = dao.getGroupById(id)
    suspend fun createGroup(req: GroupCreateRequest): GroupDto = dao.createGroup(req)
    suspend fun updateGroup(id: Int, req: GroupUpdateRequest): GroupDto? = dao.updateGroup(id, req)
    suspend fun deleteGroup(id: Int): Boolean = dao.deleteGroup(id)
}