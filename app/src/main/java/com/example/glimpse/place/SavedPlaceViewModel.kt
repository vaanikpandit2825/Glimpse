package com.example.glimpse.place

import androidx.lifecycle.ViewModel
import com.example.glimpse.firebase.FirebaseRepository
import com.example.glimpse.model.SavedPlace
import com.google.firebase.auth.FirebaseAuth

class SavedPlaceViewModel:ViewModel(){
    private val repository = FirebaseRepository()

    fun savePlace(
        place: SavedPlace,
        onSuccess: ()->Unit,
        onFailure:(Exception)->Unit
    ){
        val uid= FirebaseAuth.getInstance().currentUser?.uid

        if(uid==null){
            onFailure(Exception("User not logged in"))
            return
        }
        repository.savePlace(
            uid=uid,
            place=place,
            onSuccess=onSuccess,
            onFailure=onFailure,
        )
    }
    fun getSavedPlaces(
        onResult: (List<SavedPlace>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        repository.getSavedPlaces(
            uid = uid,
            onResult = onResult,
            onFailure = onFailure
        )
    }
}