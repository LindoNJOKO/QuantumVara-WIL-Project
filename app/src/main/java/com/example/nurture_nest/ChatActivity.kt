package com.example.nurture_nest

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatActivity : AppCompatActivity() {
    private lateinit var recyclerMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var tvChatName: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var adapter: MessageAdapter
    private val messages = mutableListOf<Message>()  // Message is your data model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerMessages = findViewById(R.id.recyclerMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        tvChatName = findViewById(R.id.tvChatName)
        btnBack = findViewById(R.id.btnBack)

        // Show chat name passed from ChatListActivity
        val chatName = intent.getStringExtra("chatName")
        tvChatName.text = chatName ?: "Chat"

        // Back button
        btnBack.setOnClickListener { finish() }

        // Setup RecyclerView with MessageAdapter
        recyclerMessages.layoutManager = LinearLayoutManager(this)
        adapter = MessageAdapter(messages)
        recyclerMessages.adapter = adapter

        // Dummy initial messages
        messages.add(Message("Hello!", true))
        messages.add(Message("Hi, how are you?", false))
        adapter.notifyDataSetChanged()

        // Send button
        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                // Add new message to list
                messages.add(Message(text, true)) // `true` = sent by user
                adapter.notifyItemInserted(messages.size - 1)

                // Scroll to bottom
                recyclerMessages.scrollToPosition(messages.size - 1)

                // Clear input
                etMessage.text.clear()
            }
        }
    }
}
