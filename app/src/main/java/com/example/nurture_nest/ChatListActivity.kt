package com.example.nurture_nest

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nurture_nest.model.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatListActivity : AppCompatActivity() {

    private lateinit var recyclerChats: RecyclerView
    private lateinit var adapter: ChatListAdapter
    private lateinit var etSearch: EditText

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val chatList = mutableListOf<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        recyclerChats = findViewById(R.id.recyclerChats)
        etSearch = findViewById(R.id.etSearch)
        val backBtn = findViewById<ImageButton>(R.id.btnBack)

        recyclerChats.layoutManager = LinearLayoutManager(this)
        adapter = ChatListAdapter(chatList) { chat ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatName", chat.name)
            intent.putExtra("receiverId", chat.id)
            startActivity(intent)
        }
        recyclerChats.adapter = adapter

        backBtn.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        loadChats()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchUsers(s.toString())
            }
        })
    }

    private fun loadChats() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("chats")
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                chatList.clear()
                for (doc in result.documents) {
                    val chatId = doc.id
                    if (chatId.contains(currentUserId)) {
                        val otherUserId = chatId.split("_").first { it != currentUserId }

                        // Get other user's info
                        db.collection("users").document(otherUserId)
                            .get()
                            .addOnSuccessListener { userDoc ->
                                val name = userDoc.getString("name") ?: "Unknown"
                                val email = userDoc.getString("email") ?: ""
                                val lastMessage = doc.getString("lastMessage") ?: ""
                                val time = doc.getLong("lastMessageTime") ?: 0L

                                val chat = Chat(
                                    id = otherUserId,
                                    name = name,
                                    email = email,
                                    lastMessage = lastMessage,
                                    time = time
                                )

                                if (!chatList.any { it.id == otherUserId }) {
                                    chatList.add(chat)
                                    chatList.sortByDescending { it.time }
                                    adapter.notifyDataSetChanged()
                                }
                            }
                    }
                }
            }
    }

    private fun searchUsers(query: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        if (query.isEmpty()) {
            loadChats()
            return
        }

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                chatList.clear()
                for (doc in result.documents) {
                    val name = doc.getString("name") ?: ""
                    val email = doc.getString("email") ?: ""
                    val id = doc.id

                    if (id != currentUserId && (name.contains(query, true) || email.contains(query, true))) {
                        chatList.add(Chat(id = id, name = name, email = email))
                    }
                }
                adapter.notifyDataSetChanged()
            }
    }
}
