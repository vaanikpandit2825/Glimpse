package com.example.glimpse.groups

import androidx.lifecycle.ViewModel
import com.example.glimpse.firebase.FirebaseRepository
import com.example.glimpse.model.GlimpseGroup
import com.example.glimpse.model.GroupMember
import com.example.glimpse.model.SharingPermissions

class GroupsViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    fun createGroup(
        group: GlimpseGroup,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.createGroup(
            group = group,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun getGroup(
        uid: String,
        onSuccess: (List<GlimpseGroup>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.getGroup(
            uid = uid,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun getGroupMembers(
        memberIds: List<String>,
        onSuccess: (List<GroupMember>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.getGroupMembers(
            memberIds = memberIds,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun addMemberToGroup(
        uid: String,
        groupId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.addMembersToGroup(
            uid = uid,
            groupId = groupId,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun removeMemberFromGroup(
        uid: String,
        groupId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.removeMemberFromGroup(
            uid = uid,
            groupId = groupId,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun deleteGroup(
        groupId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.deleteGroup(
            groupId = groupId,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun getGroupSharingPermissions(
        groupId: String,
        uid: String,
        onSuccess: (SharingPermissions) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.getGroupSharingPermissions(
            groupId = groupId,
            uid = uid,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    fun updateGroupSharingPermissions(
        groupId: String,
        uid: String,
        permissions: SharingPermissions,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        repository.updateGroupSharingPermissions(
            groupId = groupId,
            uid = uid,
            permissions = permissions,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }
}