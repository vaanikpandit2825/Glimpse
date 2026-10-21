
package com.example.glimpse.network

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object SosBackend {
    private const val BASE_URL =
        "https://glimpse-sos-backend.vaanikpandit-dev.workers.dev"
    private val client = OkHttpClient()
    suspend fun notifySos(
        senderUid: String,
        eventId: String
    ): Result<Unit> {
        return try {
            val user = FirebaseAuth.getInstance().currentUser
                ?: return Result.failure(
                    IllegalStateException("Please log in first.")
                )
            if (user.uid != senderUid) {
                return Result.failure(
                    SecurityException("SOS sender does not match logged-in user.")
                )
            }
            val idToken = user.getIdToken(false).await().token
                ?: return Result.failure(
                    IllegalStateException("Could not obtain Firebase ID token.")
                )
            val body = JSONObject()
                .put("eventId", eventId)
                .toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL/sos/notify")
                .header("Authorization", "Bearer $idToken")
                .post(body)
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.failure(
                        IllegalStateException(
                            "SOS notification request failed (HTTP ${response.code})."
                        )
                    )
                }
            }
            Result.success(Unit)
        }catch (error: Exception) {
            Result.failure(error)
        }
    }
}
