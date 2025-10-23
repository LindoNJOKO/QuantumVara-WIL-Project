package com.example.nurture_nest

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Register : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        prefs = getSharedPreferences("NurtureNestPrefs", MODE_PRIVATE)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etCellphone = findViewById<EditText>(R.id.etCellphone)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val spnRole = findViewById<Spinner>(R.id.spnRole)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnSignIn = findViewById<Button>(R.id.btnSignInNow)

        val roles = arrayOf("Parent", "Teacher", "Admin")
        spnRole.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        btnRegister.setOnClickListener {
            val name = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etCellphone.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            val role = spnRole.selectedItem.toString()

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid ?: return@addOnSuccessListener

                    val userData = hashMapOf(
                        "uid" to uid,
                        "name" to name,
                        "email" to email,
                        "cellphone" to phone,
                        "role" to role,
                        "createdAt" to Timestamp.now()
                    )

                    db.collection("users").document(uid)
                        .set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registered successfully as $role", Toast.LENGTH_SHORT).show()

                            // ✅ Automatically sign the user in after registration
                            auth.signInWithEmailAndPassword(email, pass)
                                .addOnSuccessListener {
                                    prefs.edit()
                                        .putBoolean("isLoggedIn", true)
                                        .putString("uid", uid)
                                        .putString("name", name)
                                        .putString("email", email)
                                        .putString("cellphone", phone)
                                        .putString("role", role)
                                        .apply()

                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Auto-login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, Login::class.java))
                                    finish()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error saving user: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Registration failed: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        btnSignIn.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }
}
