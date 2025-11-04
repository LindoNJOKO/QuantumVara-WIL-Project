package com.example.nurture_nest.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.nurture_nest.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var userRole: String = ""

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

        val ivPaymentIcon = view.findViewById<ImageView>(R.id.ic_payment)
        val ivLunchIcon = view.findViewById<ImageView>(R.id.ic_lunch)

        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(requireContext(), "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), Login::class.java))
            requireActivity().finish()
            return view
        }

        val uid = currentUser.uid

        // ✅ Fetch user data from Firestore
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    tvUserName.text = doc.getString("name") ?: "No Name"
                    tvUserEmail.text = doc.getString("email") ?: "No Email"
                    tvCellphone.text = doc.getString("cellphone") ?: "No Number"

                    userRole = doc.getString("role") ?: "Parent"
                    updateUIForRole(
                        tvPaymentDetails, tvLunchOrders, tvAttendanceLogs, tvChildManagement,
                        ivPaymentIcon, ivLunchIcon
                    )

                    // 🔹 Configure navigation after role is known
                    setupNavigation(tvPaymentDetails, tvLunchOrders, tvAttendanceLogs, tvChildManagement)
                } else {
                    tvUserName.text = "Unknown User"
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load profile data", Toast.LENGTH_SHORT).show()
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

    private fun setupNavigation(
        tvPaymentDetails: TextView,
        tvLunchOrders: TextView,
        tvAttendanceLogs: TextView,
        tvChildManagement: TextView
    ) {
        // 🔹 Payment
        tvPaymentDetails.setOnClickListener {
            startActivity(Intent(requireContext(), PaymentWindow::class.java))
        }

        // 🔹 Lunch Ordering
        tvLunchOrders.setOnClickListener {
            when (userRole.lowercase()) {
                "parent" -> startActivity(Intent(requireContext(), LunchOrdering::class.java))
                "admin" -> startActivity(Intent(requireContext(), AdminMealManagementActivity::class.java))
                "teacher" -> Toast.makeText(requireContext(), "Lunch management coming soon for teachers", Toast.LENGTH_SHORT).show()
            }
        }

        // 🔹 Attendance
        tvAttendanceLogs.setOnClickListener {
            val intent = Intent(requireContext(), AttendanceActivity::class.java)
            intent.putExtra("userRole", userRole)
            startActivity(intent)
        }

        // 🔹 Child Management — Role-based navigation
        when (userRole.lowercase()) {
            "teacher" -> {
                tvChildManagement.setOnClickListener {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, TeacherChildListFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }
            "admin" -> {
                tvChildManagement.setOnClickListener {
                    startActivity(Intent(requireContext(), ChildManagmentActivity::class.java))
                }
            }
            "parent" -> {
                tvChildManagement.visibility = View.GONE
            }
        }
    }

    private fun updateUIForRole(
        tvPaymentDetails: TextView,
        tvLunchOrders: TextView,
        tvAttendanceLogs: TextView,
        tvChildManagement: TextView,
        ivPaymentIcon: ImageView,
        ivLunchIcon: ImageView
    ) {
        when (userRole.lowercase()) {
            "parent" -> {
                tvPaymentDetails.visibility = View.VISIBLE
                ivPaymentIcon.visibility = View.VISIBLE
                tvLunchOrders.visibility = View.VISIBLE
                ivLunchIcon.visibility = View.VISIBLE
                tvAttendanceLogs.visibility = View.VISIBLE
                tvChildManagement.visibility = View.GONE // ❌ hide for parents

                tvAttendanceLogs.text = "View Attendance"
                tvLunchOrders.text = "Order Lunch"
            }
            "teacher" -> {
                tvPaymentDetails.visibility = View.GONE
                ivPaymentIcon.visibility = View.GONE
                tvLunchOrders.visibility = View.GONE
                ivLunchIcon.visibility = View.GONE
                tvAttendanceLogs.visibility = View.VISIBLE
                tvChildManagement.visibility = View.VISIBLE

                tvAttendanceLogs.text = "Mark Attendance"
            }
            "admin" -> {
                tvPaymentDetails.visibility = View.VISIBLE
                ivPaymentIcon.visibility = View.VISIBLE
                tvLunchOrders.visibility = View.VISIBLE
                ivLunchIcon.visibility = View.VISIBLE
                tvAttendanceLogs.visibility = View.VISIBLE
                tvChildManagement.visibility = View.VISIBLE

                tvAttendanceLogs.text = "Attendance Reports"
                tvLunchOrders.text = "Manage Meals"
            }
        }
    }
}
