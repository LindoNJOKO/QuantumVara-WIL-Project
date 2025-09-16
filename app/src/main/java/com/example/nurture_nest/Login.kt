package com.example.nurture_nest

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nurture_nest.Fragments.AdminDashboardFragment
import com.example.nurture_nest.Fragments.ParentDashboardFragment
import com.example.nurture_nest.Fragments.TeacherDashboardFragment

class Login : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        prefs = getSharedPreferences("NurtureNestPrefs", MODE_PRIVATE)

        val username = findViewById<EditText>(R.id.etLoginUsername)
        val password = findViewById<EditText>(R.id.etLoginPassword)
        val loginBtn = findViewById<Button>(R.id.btnLogin)

        loginBtn.setOnClickListener {
            val user = username.text.toString().trim()
            val pass = password.text.toString().trim()

            val savedUser = prefs.getString("username", null)
            val savedPass = prefs.getString("password", null)
            val savedRole = prefs.getString("userType", "Parent")

            if (user == savedUser && pass == savedPass) {
                prefs.edit().putBoolean("isLoggedIn", true).apply()

                Toast.makeText(this, "Welcome $savedRole!", Toast.LENGTH_SHORT).show()

                // Navigate based on role
                when (savedRole) {
                    "Parent" -> startActivity(Intent(this, ParentDashboardFragment::class.java))
                    "Teacher" -> startActivity(Intent(this, TeacherDashboardFragment::class.java))
                    "Admin" -> startActivity(Intent(this, AdminDashboardFragment::class.java))
                    else -> startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
