package com.example.glimpse

import android.app.Application
import com.example.glimpse.firebase.GlimpseDatabase
import com.google.firebase.auth.FirebaseAuth

class GlimpseDebugApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseAuth.getInstance().useEmulator(
            "127.0.0.1",
            9099
        )
        GlimpseDatabase.instance
    }
}
