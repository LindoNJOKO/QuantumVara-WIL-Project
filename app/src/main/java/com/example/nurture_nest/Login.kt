package com.example.nurture_nest

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

class Login : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        prefs = getSharedPreferences("NurtureNestPrefs", MODE_PRIVATE)

        val username = findViewById<EditText>(R.id.etLoginUsername)
        val password = findViewById<EditText>(R.id.etLoginPassword)
        val loginBtn = findViewById<Button>(R.id.btnLogin)
        val registerBtn = findViewById<Button>(R.id.btnRegisterNow)

        // Go to Register screen
        registerBtn.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        // Login logic
        loginBtn.setOnClickListener {
            val user = username.text.toString().trim()
            val pass = password.text.toString().trim()

            val savedUser = prefs.getString("username", null)
            val savedPass = prefs.getString("password", null)
            val savedRole = prefs.getString("userType", "Parent")

            if (user == savedUser && pass == savedPass) {
                prefs.edit {
                    putBoolean("isLoggedIn", true)
                }

                Toast.makeText(this, "Welcome $savedRole!", Toast.LENGTH_SHORT).show()

                // Open MainActivity after login
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
