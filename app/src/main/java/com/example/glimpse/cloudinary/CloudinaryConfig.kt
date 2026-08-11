package com.example.glimpse.cloudinary

import android.content.Context
import com.cloudinary.android.MediaManager

object CloudinaryConfig{
    private  const val  CLOUD_NAME="qlkzpatg"
    fun initialize(context: Context) {
        val config=mapOf(
        "cloud_name" to CLOUD_NAME,
        "secure" to true
        )
        MediaManager.init(context, config)
    }
}