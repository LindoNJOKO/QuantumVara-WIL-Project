package com.example.nurture_nest

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class Register : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        prefs = getSharedPreferences("NurtureNestPrefs", MODE_PRIVATE)

        val username = findViewById<EditText>(R.id.etUsername)
        val password = findViewById<EditText>(R.id.etPassword)
        val roleSpinner = findViewById<Spinner>(R.id.spnRole)
        val registerBtn = findViewById<Button>(R.id.btnRegister)
        val signInBtn = findViewById<Button>(R.id.btnSignInNow)

        // Setup role options
        val roles = arrayOf("Parent", "Teacher", "Admin")
        roleSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        signInBtn.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
        registerBtn.setOnClickListener {
            val user = username.text.toString().trim()
            val pass = password.text.toString().trim()
            val role = roleSpinner.selectedItem.toString()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter username & password", Toast.LENGTH_SHORT).show()
            } else {
                prefs.edit()
                    .putBoolean("isRegistered", true)
                    .putString("username", user)
                    .putString("password", pass)
                    .putString("userType", role)
                    .apply()

                Toast.makeText(this, "Registered as $role", Toast.LENGTH_SHORT).show()

                // After register → go to Login
                startActivity(Intent(this, Login::class.java))
                finish()


            }
        }
    }
}
