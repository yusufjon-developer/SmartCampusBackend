package com.smartcampus.features.groups

import com.smartcampus.data.repositories.GroupsRepositoryImpl
import com.smartcampus.domain.models.GroupCreateRequest
import com.smartcampus.domain.models.GroupDto
import com.smartcampus.domain.models.GroupUpdateRequest
import com.smartcampus.domain.models.common.PageRequestParams
import com.smartcampus.domain.models.common.PaginatedResult

class GroupsService(private val repo: GroupsRepositoryImpl) {
    suspend fun listGroups(params: PageRequestParams): PaginatedResult<GroupDto> = repo.listGroups(params)
    suspend fun getGroup(id: Int): GroupDto? = repo.getGroupById(id)
    suspend fun createGroup(req: GroupCreateRequest): GroupDto = repo.createGroup(req)
    suspend fun updateGroup(id: Int, req: GroupUpdateRequest): GroupDto? = repo.updateGroup(id, req)
    suspend fun deleteGroup(id: Int): Boolean = repo.deleteGroup(id)
}
