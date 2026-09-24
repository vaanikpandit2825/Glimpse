package com.example.glimpse.places

import com.example.glimpse.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.util.concurrent.Executors

data class PlaceSearchResult(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)

class PlaceSearchRepository {

    private val executor = Executors.newSingleThreadExecutor()

    fun searchPlaces(
        query: String,
        latitude: Double? = null,
        longitude: Double? = null,
        onResult: (List<PlaceSearchResult>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (query.isBlank()) {
            onResult(emptyList())
            return
        }

        executor.execute {
            try {
                val encodedQuery = URLEncoder.encode(
                    query.trim(),
                    "UTF-8"
                )

                val proximity = if (latitude != null && longitude != null) {
                    "&proximity=$longitude,$latitude"
                } else {
                    ""
                }

                val url = URL(
                    "https://api.maptiler.com/geocoding/" +
                            "$encodedQuery.json" +
                            "?autocomplete=true" +
                            "&limit=10" +
                            "&fuzzyMatch=true" +
                            "&types=poi,address,place" +
                            proximity +
                            "&key=${BuildConfig.MAPTILER_API_KEY}"
                )

                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000

                val responseCode = connection.responseCode

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception(
                        "MapTiler search failed: HTTP $responseCode"
                    )
                }

                val response = connection.inputStream
                    .bufferedReader()
                    .use { it.readText() }

                connection.disconnect()

                val json = JSONObject(response)
                val features = json.optJSONArray("features")

                val results = mutableListOf<PlaceSearchResult>()

                if (features != null) {
                    for (i in 0 until features.length()) {

                        val feature = features.getJSONObject(i)

                        val id = feature.optString("id")
                        val name = feature.optString("text")

                        val properties =
                            feature.optJSONObject("properties")

                        val placeName =
                            properties?.optString("name")
                                ?.takeIf { it.isNotBlank() }
                                ?: name

                        val address = buildAddress(feature)

                        val geometry =
                            feature.optJSONObject("geometry")

                        val coordinates =
                            geometry?.optJSONArray("coordinates")

                        if (
                            coordinates != null &&
                            coordinates.length() >= 2
                        ) {
                            val longitude =
                                coordinates.optDouble(0)

                            val latitude =
                                coordinates.optDouble(1)

                            if (
                                !longitude.isNaN() &&
                                !latitude.isNaN()
                            ) {
                                results.add(
                                    PlaceSearchResult(
                                        id = id,
                                        name = placeName,
                                        address = address,
                                        latitude = latitude,
                                        longitude = longitude
                                    )
                                )
                            }
                        }
                    }
                }

                onResult(results)

            } catch (exception: Exception) {
                onFailure(exception)
            }
        }
    }

    private fun buildAddress(
        feature: JSONObject
    ): String {

        val properties =
            feature.optJSONObject("properties")

        val context =
            feature.optJSONArray("context")

        if (context != null) {
            val parts = mutableListOf<String>()

            for (i in 0 until context.length()) {

                val item = context.optJSONObject(i)
                    ?: continue

                val text = item.optString("text")

                if (text.isNotBlank()) {
                    parts.add(text)
                }
            }

            if (parts.isNotEmpty()) {
                return parts.joinToString(", ")
            }
        }

        return properties
            ?.optString("address")
            ?.takeIf { it.isNotBlank() }
            ?: feature.optString("place_name")
    }
}