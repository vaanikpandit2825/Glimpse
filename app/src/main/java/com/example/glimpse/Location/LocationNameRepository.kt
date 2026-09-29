package com.example.glimpse.Location

import com.example.glimpse.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class LocationNameRepository {

    fun getLocationName(
        latitude: Double,
        longitude: Double,
        onResult: (String?) -> Unit
    ) {
        Thread {
            val result = try {
                findPlace(latitude, longitude)
            } catch (_: Exception) {
                null
            }

            onResult(result)
        }.start()
    }

    private fun findPlace(
        latitude: Double,
        longitude: Double
    ): String? {
        val poiUrl = buildUrl(
            latitude = latitude,
            longitude = longitude,
            types = "poi"
        )

        val poiResult = request(poiUrl)

        if (!poiResult.isNullOrBlank()) {
            return poiResult
        }

        val areaUrl = buildUrl(
            latitude = latitude,
            longitude = longitude,
            types = "locality,neighbourhood,place,municipality"
        )

        return request(areaUrl)
    }

    private fun buildUrl(
        latitude: Double,
        longitude: Double,
        types: String
    ): String {
        val encodedTypes = URLEncoder.encode(types, "UTF-8")

        return "https://api.maptiler.com/geocoding/" +
                "$longitude,$latitude.json" +
                "?key=${BuildConfig.MAPTILER_API_KEY}" +
                "&types=$encodedTypes" +
                "&limit=1"
    }

    private fun request(urlString: String): String? {
        val connection =
            URL(urlString).openConnection() as HttpURLConnection

        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode !in 200..299) {
                return null
            }

            val response =
                connection.inputStream.bufferedReader().use {
                    it.readText()
                }

            parsePlaceName(response)
        } finally {
            connection.disconnect()
        }
    }

    private fun parsePlaceName(
        response: String
    ): String? {
        val json = JSONObject(response)
        val features = json.optJSONArray("features")

        if (features == null || features.length() == 0) {
            return null
        }

        val feature = features.optJSONObject(0)
            ?: return null

        val text = feature.optString("text")

        if (text.isNotBlank()) {
            return text
        }

        val placeName = feature.optString("place_name")

        if (placeName.isBlank()) {
            return null
        }

        return placeName
            .split(",")
            .firstOrNull()
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }
}