package com.example.nurture_nest.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nurture_nest.R
import com.example.nurture_nest.model.User
import java.text.SimpleDateFormat
import java.util.*

class UserAdapter(
    private val users: List<User>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    // Map to store last messages: userId -> (message, timestamp)
    private var lastMessages: Map<String, Pair<String, Long>> = emptyMap()

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.chatName)
        val lastMessage: TextView = itemView.findViewById(R.id.chatMessage)
        val lastMessageTime: TextView = itemView.findViewById(R.id.chatTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        val (message, time) = lastMessages[user.uid] ?: Pair("No messages yet", 0L)

        holder.userName.text = user.name
        holder.lastMessage.text = message

        holder.lastMessageTime.text = if (time > 0L) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time))
        } else {
            ""
        }

        holder.itemView.setOnClickListener { onItemClick(user) }
    }

    override fun getItemCount(): Int = users.size

    fun setLastMessages(map: Map<String, Pair<String, Long>>) {
        lastMessages = map
    }
}
