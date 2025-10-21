package com.example.nurture_nest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nurture_nest.model.Chat
import java.text.SimpleDateFormat
import java.util.*

class ChatListAdapter(
    private val chats: List<Chat>,
    private val onItemClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chatName: TextView = itemView.findViewById(R.id.chatName)
        val chatMessage: TextView = itemView.findViewById(R.id.chatMessage)
        val chatTime: TextView = itemView.findViewById(R.id.chatTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.chatName.text = chat.name
        holder.chatMessage.text = if (chat.lastMessage.isNotEmpty()) chat.lastMessage else "No messages yet"
        holder.chatTime.text = if (chat.time > 0)
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(chat.time))
        else ""

        holder.itemView.setOnClickListener { onItemClick(chat) }
    }

    override fun getItemCount(): Int = chats.size
}
