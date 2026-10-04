package com.example.glimpse.weather

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class WeatherData(
    val temperature: Int,
    val description: String
)

class WeatherRepository {

    suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double
    ): WeatherData? = withContext(Dispatchers.IO) {

        try {
            val url = URL(
                "https://api.open-meteo.com/v1/forecast" +
                        "?latitude=$latitude" +
                        "&longitude=$longitude" +
                        "&current=temperature_2m,weather_code" +
                        "&temperature_unit=celsius"
            )

            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                connection.disconnect()
                return@withContext null
            }

            val response = connection.inputStream
                .bufferedReader()
                .use { it.readText() }

            connection.disconnect()

            val json = JSONObject(response)
            val current = json.getJSONObject("current")

            val temperature = current
                .getDouble("temperature_2m")
                .toInt()

            val weatherCode = current
                .getInt("weather_code")

            WeatherData(
                temperature = temperature,
                description = weatherDescription(weatherCode)
            )

        } catch (e: Exception) {
            null
        }
    }

    private fun weatherDescription(code: Int): String {
        return when (code) {
            0 -> "Clear"
            1, 2, 3 -> "Cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing drizzle"
            61, 63, 65 -> "Rain"
            66, 67 -> "Freezing rain"
            71, 73, 75, 77 -> "Snow"
            80, 81, 82 -> "Rain showers"
            85, 86 -> "Snow showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm"
            else -> "Unknown"
        }
    }
}