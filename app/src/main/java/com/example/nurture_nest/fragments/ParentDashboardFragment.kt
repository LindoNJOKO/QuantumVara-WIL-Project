package com.example.nurture_nest.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.nurture_nest.ChatListActivity
import com.example.nurture_nest.R

class ParentDashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_parent_dashboard, container, false)

        // Link Chat button
        val btnChat: ImageButton = view.findViewById(R.id.btnChat)
        btnChat.setOnClickListener {
            val intent = Intent(requireContext(), ChatListActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
