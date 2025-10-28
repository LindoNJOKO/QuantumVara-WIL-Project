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

        // Load basic Firebase user data
        etName.setText(user?.displayName ?: "")
        etEmail.setText(user?.email ?: "")

        // ✅ Always load phone from Firestore
        user?.let {
            db.collection("users").document(it.uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val phone = doc.getString("phone") ?: ""
                        etPhone.setText(phone)
                    } else {
                        // If no Firestore record exists yet, create a placeholder
                        db.collection("users").document(it.uid)
                            .set(mapOf("phone" to ""))
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to load user info", Toast.LENGTH_SHORT).show()
                }
        }

        btnSaveProfile.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newEmail = etEmail.text.toString().trim()
            val newPhone = etPhone.text.toString().trim()

            if (newName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
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
                    // Update email in Firebase Auth
                    user.updateEmail(newEmail)
                        .addOnSuccessListener {
                            // ✅ Save all fields to Firestore
                            val userData = hashMapOf(
                                "name" to newName,
                                "email" to newEmail,
                                "phone" to newPhone
                            )

                            db.collection("users").document(user.uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                    parentFragmentManager.popBackStack()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), "Failed to update Firestore: ${it.message}", Toast.LENGTH_SHORT).show()
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
