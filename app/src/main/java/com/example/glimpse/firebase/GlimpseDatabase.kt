
package com.example.glimpse.firebase

import android.content.pm.ApplicationInfo
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

object GlimpseDatabase {

    private const val PRODUCTION_URL =
        "https://glimpse-e0aab-default-rtdb.asia-southeast1.firebasedatabase.app"

    private const val EMULATOR_URL =
        "https://demo-glimpse-default-rtdb.firebaseio.com"

    val instance: FirebaseDatabase by lazy {

        val context = FirebaseApp.getInstance().applicationContext

        val isDebug =
            (context.applicationInfo.flags and
                    ApplicationInfo.FLAG_DEBUGGABLE) != 0

        val databaseUrl =
            if (isDebug) EMULATOR_URL else PRODUCTION_URL

        FirebaseDatabase.getInstance(databaseUrl).apply {
            if (isDebug) {
                useEmulator("127.0.0.1", 9000)
            }
        }
    }
}
