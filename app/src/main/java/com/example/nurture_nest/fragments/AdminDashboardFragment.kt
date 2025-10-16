package com.example.nurture_nest.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.nurture_nest.R
import com.example.nurture_nest.ChatListActivity
import com.example.nurture_nest.NotificationActivity

class AdminDashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false)

        val chatBtn = view.findViewById<ImageButton>(R.id.btnChat)
        chatBtn.setOnClickListener {
            val intent = Intent(requireContext(), ChatListActivity::class.java)
            startActivity(intent)
        }

        val notificationBtn = view.findViewById<Button>(R.id.btnAddNotification)
        notificationBtn.setOnClickListener {
            val intent = Intent(requireContext(), NotificationActivity::class.java)
            startActivity(intent)
        }
        return view
    }
}
