package com.example.nurture_nest.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nurture_nest.adapters.AnnouncementAdapter
import com.google.firebase.firestore.*
import com.example.nurture_nest.ChatListActivity
import com.example.nurture_nest.NotificationHelper
import com.example.nurture_nest.model.Announcement
import com.example.nurture_nest.R


class ParentDashboardFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null
    private var latestTimestamp: Long = 0L  // 🔹 Track latest announcement seen

    private lateinit var rv: RecyclerView
    private val adapter = AnnouncementAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_parent_dashboard, container, false)

        rv = view.findViewById(R.id.rvAnnouncements)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        val btnChat: ImageButton = view.findViewById(R.id.btnChat)
        btnChat.setOnClickListener {
            startActivity(Intent(requireContext(), ChatListActivity::class.java))
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        startAnnouncementListener()
    }

    override fun onStop() {
        super.onStop()
        listenerRegistration?.remove()
    }

    private fun startAnnouncementListener() {
        val query = db.collection("announcements")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)

        listenerRegistration = query.addSnapshotListener { snapshots, e ->
            if (e != null || snapshots == null) return@addSnapshotListener

            val all = snapshots.documents.mapNotNull { it.toObject(Announcement::class.java) }

            // 🔹 Update RecyclerView
            adapter.submitList(all)

            // 🔹 Notify only for *newer* items
            for (dc in snapshots.documentChanges) {
                if (dc.type == DocumentChange.Type.ADDED) {
                    val ann = dc.document.toObject(Announcement::class.java)
                    val annTime = (dc.document.getTimestamp("timestamp")?.toDate()?.time) ?: 0L

                    if (annTime > latestTimestamp && latestTimestamp != 0L) {
                        NotificationHelper.showLocalNotification(
                            requireContext(),
                            ann.title,
                            ann.message,
                            ann.urgent
                        )
                    }

                    // Update tracker
                    if (annTime > latestTimestamp) latestTimestamp = annTime
                }
            }
        }
    }
}



