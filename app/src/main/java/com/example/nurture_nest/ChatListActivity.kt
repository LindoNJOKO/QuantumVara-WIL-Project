package com.example.nurture_nest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatListActivity : AppCompatActivity() {
    private lateinit var recyclerChats: RecyclerView
    private lateinit var adapter: ChatListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        recyclerChats = findViewById(R.id.recyclerChats)
        recyclerChats.layoutManager = LinearLayoutManager(this)

        // Dummy chat list (you can replace with database/Firebase later)
        val chatList = listOf(
            Chat("Michael Joshua", "Hey there!", "10 min ago"),
            Chat("Lisa Moore", "See you tomorrow", "1 hr ago")
        )

        // Setup adapter with click listener
        adapter = ChatListAdapter(chatList) { chat ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatName", chat.name)
            startActivity(intent)
        }

        recyclerChats.adapter = adapter
    }
}
