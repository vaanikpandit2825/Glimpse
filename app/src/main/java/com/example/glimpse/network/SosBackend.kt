
package com.example.glimpse.network

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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
                    SecurityException(
                        "SOS sender does not match logged-in user."
                    )
                )
            }

            val idToken = user.getIdToken(false).await().token
                ?: return Result.failure(
                    IllegalStateException(
                        "Could not obtain Firebase ID token."
                    )
                )

            val body = JSONObject()
                .put("eventId", eventId)
                .toString()
                .toRequestBody(
                    "application/json; charset=utf-8".toMediaType()
                )

            val request = Request.Builder()
                .url("$BASE_URL/sos/notify")
                .header("Authorization", "Bearer $idToken")
                .post(body)
                .build()

            withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    val responseBody =
                        response.body?.string().orEmpty()

                    android.util.Log.d(
                        "GLIMPSE_SOS",
                        "Backend HTTP ${response.code}: $responseBody"
                    )

                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            IllegalStateException(
                                "SOS backend failed (HTTP ${response.code}): " +
                                        responseBody
                            )
                        )
                    }

                    val json = runCatching {
                        JSONObject(responseBody)
                    }.getOrNull()

                    val notificationStatus =
                        json?.optString("notificationStatus", "")

                    if (
                        notificationStatus == "failed" ||
                        notificationStatus == "partially_sent"
                    ) {
                        return@withContext Result.failure(
                            IllegalStateException(
                                "SOS notification status: $notificationStatus. " +
                                        responseBody
                            )
                        )
                    }

                    Result.success(Unit)
                }
            }
        } catch (error: Exception) {
            android.util.Log.e(
                "GLIMPSE_SOS",
                "SOS backend request failed",
                error
            )
            Result.failure(error)
        }
    }
}
