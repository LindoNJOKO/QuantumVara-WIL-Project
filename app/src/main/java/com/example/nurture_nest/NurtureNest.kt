package com.example.nurture_nest

import android.app.Application
import com.google.firebase.FirebaseApp

/**
 * Global Application class for Nurture Nest.
 *
 * This ensures Firebase is initialized once at app startup,
 * allowing Firestore, Auth, and other Firebase services
 * to be safely used across all activities and fragments.
 */
class NurtureNest : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase SDK
        FirebaseApp.initializeApp(this)
    }
}
