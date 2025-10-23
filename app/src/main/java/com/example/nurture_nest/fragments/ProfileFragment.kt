package com.example.nurture_nest.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.nurture_nest.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val tvUserEmail = view.findViewById<TextView>(R.id.tvUserEmail)
        val tvCellphone = view.findViewById<TextView>(R.id.tvUserPhone)

        val tvPaymentDetails = view.findViewById<TextView>(R.id.tvPaymentDetails)
        val tvLunchOrders = view.findViewById<TextView>(R.id.tvLunchOrders)
        val tvAttendanceLogs = view.findViewById<TextView>(R.id.tvAttendanceLogs)
        val tvChildManagement = view.findViewById<TextView>(R.id.tvChildManagement)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        val currentUser = auth.currentUser

        // 🔒 Check if user is logged in
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), Login::class.java))
            requireActivity().finish()
            return view
        }

        val uid = currentUser.uid

        // ✅ Safely fetch user data
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    tvUserName.text = doc.getString("name") ?: "No Name"
                    tvUserEmail.text = doc.getString("email") ?: "No Email"
                    tvCellphone.text = doc.getString("cellphone") ?: "No Number"
                } else {
                    tvUserName.text = "Unknown User"
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load profile data", Toast.LENGTH_SHORT).show()
            }

        // 🔹 Payment Details
        tvPaymentDetails.setOnClickListener {
            startActivity(Intent(requireContext(), PaymentWindow::class.java))
        }

        // 🔹 Lunch Ordering
        tvLunchOrders.setOnClickListener {
            startActivity(Intent(requireContext(), LunchOrdering::class.java))
        }

        // 🔹 Attendance Logs
        tvAttendanceLogs.setOnClickListener {
            startActivity(Intent(requireContext(), AttendanceActivity::class.java))
        }

        // 🔹 Child Management
        tvChildManagement.setOnClickListener {
            startActivity(Intent(requireContext(), ChildManagmentActivity::class.java))
        }

        // 🔹 Logout
        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), Login::class.java))
            requireActivity().finish()
        }

        return view
    }
}
