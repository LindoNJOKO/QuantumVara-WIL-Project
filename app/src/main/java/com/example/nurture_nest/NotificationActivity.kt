package com.example.nurture_nest

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import com.example.nurture_nest.fragments.AdminDashboardFragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class NotificationActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etMessage = findViewById<EditText>(R.id.etMessage)
        val switchUrgent = findViewById<SwitchMaterial>(R.id.switchUrgent)
        val btnPost = findViewById<Button>(R.id.btnPost)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val btnHome = findViewById<Button>(R.id.btnHome)

        btnHome.setOnClickListener {
            val intent = Intent(this, AdminDashboardFragment::class.java)
            startActivity(intent)
        }

        btnPost.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val message = etMessage.text.toString().trim()
            val urgent = switchUrgent.isChecked

            if (title.isEmpty() || message.isEmpty()) {
                tvStatus.text = "Title and message are required."
                return@setOnClickListener
            }

            val data = hashMapOf(
                "title" to title,
                "message" to message,
                "timestamp" to FieldValue.serverTimestamp(),
                "urgent" to urgent,
                "createdBy" to "Admin"
            )
            btnPost.isEnabled = false
            db.collection("announcements")
                .add(data)
                .addOnSuccessListener {
                    tvStatus.text = "Announcement posted."
                    etTitle.text.clear()
                    etMessage.text.clear()
                    switchUrgent.isChecked = false
                    btnPost.isEnabled = true
                }
                .addOnFailureListener { e ->
                    tvStatus.text = "Failed: ${e.message}"
                    btnPost.isEnabled = true
                }
        }
    }
}