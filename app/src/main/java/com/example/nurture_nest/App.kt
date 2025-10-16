package com.example.nurture_nest

import android.app.Application
import com.google.firebase.FirebaseApp

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        NotificationHelper.createNotificationChannel(this)
    }
}