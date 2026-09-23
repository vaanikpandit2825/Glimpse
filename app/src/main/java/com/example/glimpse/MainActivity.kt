package com.example.glimpse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.glimpse.cloudinary.CloudinaryConfig
import com.example.glimpse.navigation.AppNavigation
import com.example.glimpse.ui.theme.GlimpseTheme
import com.google.android.libraries.places.api.Places

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CloudinaryConfig.initialize(this)

        setContent {
            GlimpseTheme {
                AppNavigation()
            }
        }
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(
                applicationContext,
                BuildConfig.GOOGLE_PLACES_API_KEY
            )
        }
    }
}