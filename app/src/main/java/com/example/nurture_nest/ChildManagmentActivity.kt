package com.example.nurture_nest

import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChildManagmentActivity : AppCompatActivity() {

    private lateinit var etPreferredName: EditText
    private lateinit var etAllergies: EditText
    private lateinit var etMedicalConditions: EditText
    private lateinit var etVisionNeeds: EditText
    private lateinit var etEmergencyContactName: EditText
    private lateinit var etEmergencyContactNumber: EditText
    private lateinit var etAdditionalNotes: EditText
    private lateinit var btnSaveChildDetails: Button
    private lateinit var etSearchChildId: EditText
    private lateinit var btnSearchChild: Button

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "testUser"
    private var userRole = "parent"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_child_managment)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        etPreferredName = findViewById(R.id.etPreferredName)
        etAllergies = findViewById(R.id.etAllergies)
        etMedicalConditions = findViewById(R.id.etMedicalConditions)
        etVisionNeeds = findViewById(R.id.etVisionNeeds)
        etEmergencyContactName = findViewById(R.id.etEmergencyContactName)
        etEmergencyContactNumber = findViewById(R.id.etEmergencyContactNumber)
        etAdditionalNotes = findViewById(R.id.etAdditionalNotes)
        btnSaveChildDetails = findViewById(R.id.btnSaveChildDetails)
        etSearchChildId = findViewById(R.id.etSearchChildId)
        btnSearchChild = findViewById(R.id.btnSearchChild)

        // Get user role
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                userRole = doc.getString("role") ?: "parent"
                if (userRole == "admin") {
                    setupAdminView()
                } else {
                    setupParentView()
                }
            }
            .addOnFailureListener {
                setupParentView()
            }
    }

    // ✅ Parent view: can write/update
    private fun setupParentView() {
        etSearchChildId.visibility = EditText.GONE
        btnSearchChild.visibility = Button.GONE

        btnSaveChildDetails.setOnClickListener {
            val childData = hashMapOf(
                "preferredName" to etPreferredName.text.toString().trim(),
                "allergies" to etAllergies.text.toString().trim(),
                "medicalConditions" to etMedicalConditions.text.toString().trim(),
                "visionNeeds" to etVisionNeeds.text.toString().trim(),
                "emergencyContactName" to etEmergencyContactName.text.toString().trim(),
                "emergencyContactNumber" to etEmergencyContactNumber.text.toString().trim(),
                "additionalNotes" to etAdditionalNotes.text.toString().trim()
            )

            db.collection("childDetails").document(userId)
                .set(childData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Child details saved!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error saving details.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // 👀 Admin view: can search & read only
    private fun setupAdminView() {
        btnSaveChildDetails.isEnabled = false
        btnSaveChildDetails.text = "Read-Only Mode"

        etPreferredName.isEnabled = false
        etAllergies.isEnabled = false
        etMedicalConditions.isEnabled = false
        etVisionNeeds.isEnabled = false
        etEmergencyContactName.isEnabled = false
        etEmergencyContactNumber.isEnabled = false
        etAdditionalNotes.isEnabled = false

        etSearchChildId.visibility = EditText.VISIBLE
        btnSearchChild.visibility = Button.VISIBLE

        btnSearchChild.setOnClickListener {
            val searchId = etSearchChildId.text.toString().trim()
            if (searchId.isEmpty()) {
                Toast.makeText(this, "Enter child/parent ID to search", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("childDetails").document(searchId).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        etPreferredName.setText(doc.getString("preferredName"))
                        etAllergies.setText(doc.getString("allergies"))
                        etMedicalConditions.setText(doc.getString("medicalConditions"))
                        etVisionNeeds.setText(doc.getString("visionNeeds"))
                        etEmergencyContactName.setText(doc.getString("emergencyContactName"))
                        etEmergencyContactNumber.setText(doc.getString("emergencyContactNumber"))
                        etAdditionalNotes.setText(doc.getString("additionalNotes"))
                    } else {
                        Toast.makeText(this, "No record found for this ID.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Search failed.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
