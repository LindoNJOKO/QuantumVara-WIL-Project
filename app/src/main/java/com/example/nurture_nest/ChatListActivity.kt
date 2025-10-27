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

        loadAllUsers() // 🔹 Show all users when opening

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchUsers(s.toString())
            }
        })
    }

    /**
     * Load all users (except current one) and show their latest chat info if any
     */
    private fun loadAllUsers() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                chatList.clear()

                for (doc in result.documents) {
                    val userId = doc.id
                    if (userId == currentUserId) continue

                    val name = doc.getString("name") ?: "Unknown"
                    val email = doc.getString("email") ?: ""

                    val chatId = getChatId(currentUserId, userId)
                    db.collection("chats").document(chatId)
                        .get()
                        .addOnSuccessListener { chatDoc ->
                            val lastMessage = chatDoc.getString("lastMessage") ?: ""
                            val lastTime = chatDoc.getLong("lastMessageTime") ?: 0L

                            val chat = Chat(
                                id = userId,
                                name = name,
                                email = email,
                                lastMessage = lastMessage,
                                time = lastTime
                            )
                            chatList.add(chat)
                            chatList.sortByDescending { it.time }
                            adapter.notifyDataSetChanged()
                        }
                }
            }
    }

    /**
     * Search among all users by name or email
     */
    private fun searchUsers(query: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        if (query.isEmpty()) {
            loadAllUsers()
            return
        }

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                chatList.clear()
                for (doc in result.documents) {
                    val userId = doc.id
                    if (userId == currentUserId) continue

                    val name = doc.getString("name") ?: ""
                    val email = doc.getString("email") ?: ""

                    if (name.contains(query, true) || email.contains(query, true)) {
                        val chatId = getChatId(currentUserId, userId)
                        db.collection("chats").document(chatId)
                            .get()
                            .addOnSuccessListener { chatDoc ->
                                val lastMessage = chatDoc.getString("lastMessage") ?: ""
                                val lastTime = chatDoc.getLong("lastMessageTime") ?: 0L

                                val chat = Chat(
                                    id = userId,
                                    name = name,
                                    email = email,
                                    lastMessage = lastMessage,
                                    time = lastTime
                                )
                                chatList.add(chat)
                                chatList.sortByDescending { it.time }
                                adapter.notifyDataSetChanged()
                            }
                    }
                }
            }
    }

    private fun getChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }
}
