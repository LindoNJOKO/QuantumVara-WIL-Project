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
import com.example.nurture_nest.Fragments.AdminDashboardFragment
import com.example.nurture_nest.Fragments.ParentDashboardFragment
import com.example.nurture_nest.Fragments.TeacherDashboardFragment

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
                startActivity(Intent(this, Register::class.java))
            }
            !isLoggedIn -> {
                startActivity(Intent(this, Login::class.java))
            }
            else -> {
                // Already logged in → go to role-specific dashboard
                when (role) {
                    "Parent" -> startActivity(Intent(this, ParentDashboardFragment::class.java))
                    "Teacher" -> startActivity(Intent(this, TeacherDashboardFragment::class.java))
                    "Admin" -> startActivity(Intent(this, AdminDashboardFragment::class.java))
                    else -> startActivity(Intent(this, MainActivity::class.java))
                }
            }
        }
        finish()
    }
}
