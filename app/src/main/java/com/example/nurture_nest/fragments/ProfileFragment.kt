package com.example.nurture_nest.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.nurture_nest.Login
import com.example.nurture_nest.LunchOrdering
import com.example.nurture_nest.PaymentWindow
import com.example.nurture_nest.R

class ProfileFragment : Fragment() {

    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Access shared preferences
        prefs = requireActivity().getSharedPreferences("NurtureNestPrefs", 0)

        // --- Logout button ---
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            val intent = Intent(requireContext(), Login::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        // --- Payment Details ---
        val tvPaymentDetails = view.findViewById<TextView>(R.id.tvPaymentDetails)
        tvPaymentDetails.setOnClickListener {
            val intent = Intent(requireContext(), PaymentWindow::class.java)
            startActivity(intent)
        }

        // --- Lunch Orders ---
        val tvLunchOrders = view.findViewById<TextView>(R.id.tvLunchOrders)
        tvLunchOrders.setOnClickListener {
            val intent = Intent(requireContext(), LunchOrdering::class.java)
            startActivity(intent)
        }

        return view
    }
}
