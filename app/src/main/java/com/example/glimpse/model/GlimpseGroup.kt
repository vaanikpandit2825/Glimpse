package com.example.glimpse.model

data class GlimpseGroup(
    val id: String="",
    val name:String="",
    val type:String="",
    val createdBy:String="",
    val createdAt:Long=0L,
    val members:Map<String,Boolean> = emptyMap()
)