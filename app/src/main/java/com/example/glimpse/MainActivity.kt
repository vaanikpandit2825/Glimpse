
package com.example.glimpse

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.glimpse.cloudinary.CloudinaryConfig
import com.example.glimpse.navigation.AppNavigation
import com.example.glimpse.ui.theme.GlimpseTheme
import com.google.android.libraries.places.api.Places

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermission()
        CloudinaryConfig.initialize(this)

        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(
                applicationContext,
                BuildConfig.GOOGLE_PLACES_API_KEY
            )
        }

        setContent {
            GlimpseTheme {
                AppNavigation(
                    incomingSenderUid = intent.getStringExtra("senderUid"),
                    incomingEventId = intent.getStringExtra("eventId")
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val senderUid = intent.getStringExtra("senderUid")
        val eventId = intent.getStringExtra("eventId")

        if (!senderUid.isNullOrBlank() && !eventId.isNullOrBlank()) {
            // Forward the notification tap to the existing navigation flow.
            // AppNavigation will handle navigation in the next step.
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS

            if (
                ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(permission)
            }
        }
    }
}
