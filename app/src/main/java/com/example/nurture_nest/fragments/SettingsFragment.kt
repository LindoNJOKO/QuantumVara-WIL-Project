package com.example.nurture_nest.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.nurture_nest.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

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

        val sharedPref = requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

        // --- Load saved settings ---
        switchNotifications.isChecked = sharedPref.getBoolean("notifications_enabled", true)
        switchVibration.isChecked = sharedPref.getBoolean("vibration_enabled", true)
        switchSound.isChecked = sharedPref.getBoolean("sound_enabled", true)

        // --- Set up listeners ---
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("notifications_enabled", isChecked).apply()
            Toast.makeText(requireContext(),
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

        btnDeleteAccount.setOnClickListener { showReauthDialog() }

        // --- Extra: Privacy & Support ---
        view.findViewById<TextView>(R.id.tvPrivacySecurity)?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PrivacySecurityFragment())
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<TextView>(R.id.tvSupportHelp)?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SupportHelpFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun showThemeDialog(sharedPref: SharedPreferences) {
        val options = arrayOf("Light", "Dark", "System Default")
        val currentMode = sharedPref.getInt("app_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        val checkedItem = when (currentMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> 0
            AppCompatDelegate.MODE_NIGHT_YES -> 1
            else -> 2
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Choose Theme")
            .setSingleChoiceItems(options, checkedItem) { dialogInterface, which ->
                val mode = when (which) {
                    0 -> AppCompatDelegate.MODE_NIGHT_NO
                    1 -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }

                sharedPref.edit().putInt("app_theme", mode).apply()
                AppCompatDelegate.setDefaultNightMode(mode)
                (requireActivity() as AppCompatActivity).delegate.applyDayNight()

                // ✅ Don’t restart or reload fragment
                AppCompatDelegate.setDefaultNightMode(mode)
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .create()

        // ✅ Prevent window leaks by showing only when safe
        if (isAdded && !requireActivity().isFinishing) dialog.show()
    }

    private fun showReauthDialog() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val input = EditText(requireContext()).apply {
            hint = "Enter your password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
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

                val email = user.email ?: return@setPositiveButton
                val credential = EmailAuthProvider.getCredential(email, password)

                user.reauthenticate(credential)
                    .addOnSuccessListener { deleteUserAccount() }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(),
                            "Authentication failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteUserAccount() {
        FirebaseAuth.getInstance().currentUser?.delete()
            ?.addOnSuccessListener {
                Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_LONG).show()
                val intent = Intent(requireContext(), com.example.nurture_nest.Login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            ?.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete account: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
