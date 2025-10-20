package com.example.nurture_nest.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.nurture_nest.R

class ChangePasswordFragment : Fragment() {

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_password, container, false)

        val etCurrentPassword: EditText = view.findViewById(R.id.etCurrentPassword)
        val etNewPassword: EditText = view.findViewById(R.id.etNewPassword)
        val etConfirmPassword: EditText = view.findViewById(R.id.etConfirmPassword)
        val btnChangePassword: Button = view.findViewById(R.id.btnChangePasswordFinal)

        btnChangePassword.setOnClickListener {
            val current = etCurrentPassword.text.toString().trim()
            val newPass = etNewPassword.text.toString().trim()
            val confirm = etConfirmPassword.text.toString().trim()

            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (newPass != confirm) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show()
                // TODO: Update password in your backend or database
            }
        }

        return view
    }
}
