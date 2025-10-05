package com.example.nurture_nest

import android.app.Application
import com.google.firebase.FirebaseApp

class NurtureNest : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase when app starts
        FirebaseApp.initializeApp(this)
    }
}
