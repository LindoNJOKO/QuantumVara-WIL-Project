package com.example.nurture_nest

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashScreen : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        prefs = getSharedPreferences("NurtureNestPrefs", MODE_PRIVATE)

        Handler(Looper.getMainLooper()).postDelayed({
            navigateNext()
        }, 2000)
    }

    private fun navigateNext() {
        val isRegistered = prefs.getBoolean("isRegistered", false)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
        val role = prefs.getString("userType", null)

        val nextIntent = when {
            !isRegistered -> Intent(this, Register::class.java)
            !isLoggedIn -> Intent(this, Login::class.java)
            else -> when (role) {
                "Parent" -> Intent(this, MainActivity::class.java).apply {
                    putExtra("fragmentToLoad", "ParentDashboardFragment")
                }
                "Teacher" -> Intent(this, MainActivity::class.java).apply {
                    putExtra("fragmentToLoad", "TeacherDashboardFragment")
                }
                "Admin" -> Intent(this, MainActivity::class.java).apply {
                    putExtra("fragmentToLoad", "AdminDashboardFragment")
                }
                else -> Intent(this, MainActivity::class.java)
            }
        }

        startActivity(nextIntent)
        finish()
    }
}