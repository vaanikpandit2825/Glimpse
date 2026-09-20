package com.example.glimpse.model

data class SavedPlace(
    val id:String="",
    val name:String="",
    val type:String="",
    val latitude:Double=0.0,
    val longtitude: Double=0.0,
    val radius:Double=200.0,
    val createdAt:Long=0L
)