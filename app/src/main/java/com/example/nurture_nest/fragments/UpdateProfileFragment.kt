package com.example.nurture_nest.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.nurture_nest.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class UpdateProfileFragment : Fragment() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnSaveProfile: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_update_profile, container, false)

        etName = view.findViewById(R.id.etName)
        etEmail = view.findViewById(R.id.etEmail)
        etPhone = view.findViewById(R.id.etPhone)
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile)

        val user = auth.currentUser

        // Load existing data
        etName.setText(user?.displayName ?: "")
        etEmail.setText(user?.email ?: "")

        // Load cellphone number from Firestore
        user?.let {
            db.collection("users").document(it.uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val phone = doc.getString("cellphone") ?: ""
                        etPhone.setText(phone)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to load user info", Toast.LENGTH_SHORT).show()
                }
        }

        // Save button logic
        btnSaveProfile.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newEmail = etEmail.text.toString().trim()
            val newPhone = etPhone.text.toString().trim()

            // 🧠 Basic validations
            when {
                newName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty() -> {
                    Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                newPhone.length != 10 -> {
                    Toast.makeText(requireContext(), "Invalid Phone number. Phone number can only be 10 digits", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                !newPhone.matches(Regex("^\\d{10}$")) -> {
                    Toast.makeText(requireContext(), "Phone number must contain only digits", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            if (user == null) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update display name in Firebase Auth
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            user.updateProfile(profileUpdates)
                .addOnSuccessListener {
                    user.updateEmail(newEmail)
                        .addOnSuccessListener {
                            // Save to Firestore (consistent with your database field names)
                            val data = mapOf(
                                "name" to newName,
                                "email" to newEmail,
                                "cellphone" to newPhone
                            )

                            db.collection("users").document(user.uid)
                                .update(data)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                    parentFragmentManager.popBackStack()
                                }
                                .addOnFailureListener {
                                    // If document doesn't exist, create it
                                    db.collection("users").document(user.uid)
                                        .set(data)
                                        .addOnSuccessListener {
                                            Toast.makeText(requireContext(), "Profile created successfully", Toast.LENGTH_SHORT).show()
                                            parentFragmentManager.popBackStack()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(requireContext(), "Firestore save failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to update email: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to update name: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        return view
    }
}
