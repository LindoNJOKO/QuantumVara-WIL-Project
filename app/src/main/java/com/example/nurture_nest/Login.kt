package com.example.nurture_nest

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Login : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        prefs = getSharedPreferences("NurtureNestPrefs", MODE_PRIVATE)

        val username = findViewById<EditText>(R.id.etLoginUsername)
        val password = findViewById<EditText>(R.id.etLoginPassword)
        val loginBtn = findViewById<Button>(R.id.btnLogin)
        val registerBtn = findViewById<Button>(R.id.btnRegister) // ✅ Register button

        // 🔹 Login button logic
        loginBtn.setOnClickListener {
            try {
                val user = username.text.toString().trim()
                val pass = password.text.toString().trim()

                val savedUser = prefs.getString("username", null)
                val savedPass = prefs.getString("password", null)
                val savedRole = prefs.getString("userType", "Parent")

                Log.d("LoginDebug", "User input: $user, Saved user: $savedUser")
                Log.d("LoginDebug", "Role: $savedRole")

                if (user == savedUser && pass == savedPass) {
                    prefs.edit().putBoolean("isLoggedIn", true).apply()

                    // Save role to another pref for MainActivity
                    val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    sharedPref.edit().putString("user_role", savedRole).apply()

                    Toast.makeText(this, "Welcome $savedRole!", Toast.LENGTH_SHORT).show()

                    // ✅ Always start MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("LoginError", "Login failed: ${e.message}", e)
                Toast.makeText(this, "Something went wrong: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // 🔹 Register button logic
        registerBtn.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }
}
