package com.example.glimpse.cloudinary

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

class CloudinaryRepository {

    companion object {
        private const val UPLOAD_PRESET = "glimpse_profile"
    }

    fun uploadProfilePhoto(
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {

        MediaManager.get()
            .upload(imageUri)
            .unsigned(UPLOAD_PRESET)
            .callback(object : UploadCallback {

                override fun onStart(requestId: String) {
                }

                override fun onProgress(
                    requestId: String,
                    bytes: Long,
                    totalBytes: Long
                ) {
                }

                override fun onSuccess(
                    requestId: String,
                    resultData: Map<*, *>
                ) {
                    val secureUrl = resultData["secure_url"] as? String

                    if (secureUrl != null) {
                        onSuccess(secureUrl)
                    } else {
                        onFailure("Cloudinary did not return an image URL")
                    }
                }

                override fun onError(
                    requestId: String,
                    error: ErrorInfo
                ) {
                    onFailure(error.description ?: "Upload failed")
                }

                override fun onReschedule(
                    requestId: String,
                    error: ErrorInfo
                ) {
                    onFailure(
                        error.description ?: "Upload rescheduled"
                    )
                }
            })
            .dispatch()
    }
}