package com.example.nurture_nest.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.example.nurture_nest.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import android.widget.EditText

class SettingsFragment : Fragment() {

    private lateinit var switchNotifications: Switch
    private lateinit var switchVibration: Switch
    private lateinit var switchSound: Switch
    private lateinit var cardThemeSettings: MaterialCardView

    private lateinit var btnUpdateProfile: MaterialButton
    private lateinit var btnChangePassword: MaterialButton
    private lateinit var btnDeleteAccount: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // --- Find views ---
        switchNotifications = view.findViewById(R.id.switchNotifications)
        switchVibration = view.findViewById(R.id.switchVibration)
        switchSound = view.findViewById(R.id.switchSound)
        cardThemeSettings = view.findViewById(R.id.cardThemeSettings)

        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile)
        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount)

        // --- SharedPreferences for settings ---
        val sharedPref = requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

        // Load saved notification settings
        switchNotifications.isChecked = sharedPref.getBoolean("notifications_enabled", true)
        switchVibration.isChecked = sharedPref.getBoolean("vibration_enabled", true)
        switchSound.isChecked = sharedPref.getBoolean("sound_enabled", true)

        // Apply saved theme
        val currentMode = sharedPref.getInt("app_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(currentMode)

        // --- Set listeners for notification settings ---
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("notifications_enabled", isChecked).apply()
            Toast.makeText(
                requireContext(),
                if (isChecked) "Notifications enabled" else "Notifications disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("vibration_enabled", isChecked).apply()
        }

        switchSound.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("sound_enabled", isChecked).apply()
        }

        // --- Theme selection dialog ---
        cardThemeSettings.setOnClickListener {
            showThemeDialog(sharedPref)
        }

        // --- Navigation buttons ---
        btnUpdateProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UpdateProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        btnChangePassword.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChangePasswordFragment())
                .addToBackStack(null)
                .commit()
        }

        btnDeleteAccount.setOnClickListener {
            showReauthDialog()
        }

        // --- NEW: TextViews for Privacy & Support ---
        val tvPrivacySecurity = view.findViewById<TextView>(R.id.tvPrivacySecurity)
        val tvSupportHelp = view.findViewById<TextView>(R.id.tvSupportHelp)

        tvPrivacySecurity.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .replace(R.id.fragment_container, PrivacySecurityFragment())
                .addToBackStack(null)
                .commit()
        }

        tvSupportHelp.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .replace(R.id.fragment_container, SupportHelpFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun showThemeDialog(sharedPref: android.content.SharedPreferences) {
        val options = arrayOf("Light", "Dark", "System Default")

        // Determine which is currently selected
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

                sharedPref.edit().putInt("app_theme", mode).apply()
                AppCompatDelegate.setDefaultNightMode(mode)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                Toast.makeText(requireContext(), "Account deleted (not implemented yet)", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showReauthDialog() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Toast.makeText(requireContext(), "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val input = EditText(requireContext()).apply {
            hint = "Enter your password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Reauthenticate")
            .setMessage("Please confirm your password to delete your account.")
            .setView(input)
            .setPositiveButton("Confirm") { _, _ ->
                val password = input.text.toString().trim()

                if (password.isEmpty()) {
                    Toast.makeText(requireContext(), "Password required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val email = user.email
                if (email.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Email not found for user", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val credential = EmailAuthProvider.getCredential(email, password)

                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        deleteUserAccount()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Authentication failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteUserAccount() {
        val user = FirebaseAuth.getInstance().currentUser
        // TODO: Add actual delete logic
    }
}
