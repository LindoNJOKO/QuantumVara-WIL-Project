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
import com.example.nurture_nest.AnnouncementAdapter
import com.google.firebase.firestore.*
import com.example.nurture_nest.ChatListActivity
import com.example.nurture_nest.NotificationHelper
import com.example.nurture_nest.Announcement
import com.example.nurture_nest.R


class ParentDashboardFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    private lateinit var rv: RecyclerView
    private val adapter = AnnouncementAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_parent_dashboard, container, false)

        // Initialize RecyclerView
        rv = view.findViewById(R.id.rvAnnouncements)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // Link Chat button
        val btnChat: ImageButton = view.findViewById(R.id.btnChat)
        btnChat.setOnClickListener {
            val intent = Intent(requireContext(), ChatListActivity::class.java)
            startActivity(intent)
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
            if (e != null) {
                Log.w("ParentDashboardFragment", "listen:error", e)
                return@addSnapshotListener
            }
            if (snapshots == null) return@addSnapshotListener

            for (dc in snapshots.documentChanges) {
                if (dc.type == DocumentChange.Type.ADDED) {
                    val ann = dc.document.toObject(Announcement::class.java)
                    // show local notification for newly added docs
                    NotificationHelper.showLocalNotification(
                        requireContext(),
                        ann.title,
                        ann.message,
                        ann.urgent
                    )
                }
            }

            // update recycler view with all docs (simple approach)
            val all = snapshots.documents.map {
                it.toObject(Announcement::class.java)!!.apply {
                    // nothing
                }
            }
            adapter.submitList(all)
        }
    }
}


