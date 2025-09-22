package com.example.nurture_nest.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.nurture_nest.Login
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

        // Find logout button
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // Handle logout logic
        btnLogout.setOnClickListener {
            // Clear stored session/user data
            prefs.edit().clear().apply()

            // Redirect to login screen
            val intent = Intent(requireContext(), Login::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        return view
    }
}