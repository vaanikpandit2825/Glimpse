package com.example.glimpse.model

import androidx.lifecycle.ViewModel
import com.example.glimpse.firebase.FirebaseRepository
import com.example.glimpse.model.GlimpseGroup
import com.example.glimpse.ui.theme.Success

class GroupsViewModel : ViewModel(){
    private val repository= FirebaseRepository()

    fun creategroup(
        group: GlimpseGroup,
        onSuccess:()->Unit,
        onFailure:(Exception)->Unit
    ){
        repository.createGroup(
            group=group,
            onSuccess= onSuccess,
            onFailure=onFailure
        )
    }
    fun getGroup(
        uid:String,
        onSuccess:(List<GlimpseGroup>)-> Unit,
        onFailure:(Exception)->Unit
    ){
        repository.getGroup(
            uid=uid,
            onSuccess=onSuccess,
            onFailure=onFailure
        )
    }
    fun addMemberToGroup(
        uid:String,
        groupId:String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ){
        repository.addMembersToGroup(
            uid=uid,
            groupId = groupId,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }
    fun removeMemberFromGroup(
        uid:String,
        groupId:String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ){
        repository.removeMemberFromGroup(
            uid=uid,
            groupId = groupId,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }
    fun deleteGroup(
        groupId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ){
        repository.deleteGroup(
            groupId=groupId,
            onSuccess=onSuccess,
            onFailure=onFailure
        )
    }
}