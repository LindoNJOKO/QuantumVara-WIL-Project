package com.example.nurture_nest.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.example.nurture_nest.R

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Theme settings row
        val tvThemeSettings: TextView = view.findViewById(R.id.tvThemeSettings)

        // Load saved theme from SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val currentMode = sharedPref.getInt("app_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(currentMode)

        tvThemeSettings.setOnClickListener {
            showThemeDialog(sharedPref)
        }

        return view
    }

    private fun showThemeDialog(sharedPref: android.content.SharedPreferences) {
        val options = arrayOf("Light", "Dark", "System Default")

        // Figure out current selected theme
        val currentMode = sharedPref.getInt("app_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        val checkedItem = when (currentMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> 0
            AppCompatDelegate.MODE_NIGHT_YES -> 1
            else -> 2
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Choose Theme")
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                val mode = when (which) {
                    0 -> AppCompatDelegate.MODE_NIGHT_NO
                    1 -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }

                // Save preference
                sharedPref.edit().putInt("app_theme", mode).apply()

                // Apply theme immediately
                AppCompatDelegate.setDefaultNightMode(mode)

                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
