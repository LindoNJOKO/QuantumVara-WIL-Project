package com.example.nurture_nest.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nurture_nest.model.Announcement
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import com.example.nurture_nest.R
import java.util.*

class AnnouncementAdapter : ListAdapter<Announcement, AnnouncementAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Announcement>() {
            override fun areItemsTheSame(oldItem: Announcement, newItem: Announcement): Boolean {
                return oldItem.title == newItem.title && oldItem.timestamp == newItem.timestamp
            }

            override fun areContentsTheSame(oldItem: Announcement, newItem: Announcement): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_announcement, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position)) // ✅ works as long as type parameters and imports are correct
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
        private val tvMessage = itemView.findViewById<TextView>(R.id.tvMessage)
        private val tvTime = itemView.findViewById<TextView>(R.id.tvTime)

        fun bind(a: Announcement) {
            tvTitle.text = a.title
            tvMessage.text = a.message
            val ts: Timestamp? = a.timestamp
            val formatted = ts?.let {
                SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(it.toDate())
            } ?: "Just now"
            tvTime.text = formatted
        }
    }
}
