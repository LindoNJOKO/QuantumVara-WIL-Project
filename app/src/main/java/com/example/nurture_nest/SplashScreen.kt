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

        // Adjust for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        prefs = getSharedPreferences("NurtureNestPrefs", MODE_PRIVATE)

        // Delay splash for 2 seconds before navigating
        Handler(Looper.getMainLooper()).postDelayed({
            navigateNext()
        }, 2000)
    }

    private fun navigateNext() {
        val isRegistered = prefs.getBoolean("isRegistered", false)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
        val role = prefs.getString("userType", null)

        when {
            !isRegistered -> {
                // First run → go to Register
                startActivity(Intent(this, Register::class.java))
            }
            !isLoggedIn -> {
                // Not logged in → go to Login
                startActivity(Intent(this, Login::class.java))
            }
            else -> {
                // Already logged in → always go to MainActivity with role
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("userType", role)
                startActivity(intent)
            }
        }
        finish()
    }
}
