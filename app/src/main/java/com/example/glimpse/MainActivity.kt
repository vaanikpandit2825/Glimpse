
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {

    private var incomingSenderUid by mutableStateOf<String?>(null)
    private var incomingEventId by mutableStateOf<String?>(null)
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

        incomingSenderUid = intent.getStringExtra("senderUid")
        incomingEventId = intent.getStringExtra("eventId")

        setContent {
            GlimpseTheme {
                AppNavigation(
                    incomingSenderUid = incomingSenderUid,
                    incomingEventId = incomingEventId
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
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
