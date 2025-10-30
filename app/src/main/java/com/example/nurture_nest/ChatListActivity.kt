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
import com.example.nurture_nest.adapters.UserAdapter
import com.example.nurture_nest.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var btnBack: ImageButton
    private lateinit var adapter: UserAdapter

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val users = mutableListOf<User>()
    private val filteredUsers = mutableListOf<User>()
    private val lastMessages = mutableMapOf<String, Pair<String, Long>>() // userId -> (message, time)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        recyclerView = findViewById(R.id.recyclerChats)
        searchBar = findViewById(R.id.etSearch)
        btnBack = findViewById(R.id.btnBack)

        adapter = UserAdapter(filteredUsers) { selectedUser ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("receiverId", selectedUser.uid)
            intent.putExtra("chatName", selectedUser.name)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnBack.setOnClickListener { finish() }

        loadUsersBasedOnRole()

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadUsersBasedOnRole() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { currentUserDoc ->
                val currentRole = currentUserDoc.getString("role")?.lowercase() ?: "parent"
                val targetRole = if (currentRole == "parent") "admin" else "parent"

                db.collection("users")
                    .whereEqualTo("role", targetRole.capitalize()) // 🔥 match capitalized role in Firestore
                    .get()
                    .addOnSuccessListener { userSnapshot ->
                        users.clear()
                        lastMessages.clear()

                        for (userDoc in userSnapshot.documents) {
                            val user = userDoc.toObject(User::class.java)
                            if (user != null) {
                                users.add(user)

                                val chatId = generateChatId(currentUserId, user.uid)
                                db.collection("chats").document(chatId)
                                    .collection("messages")
                                    .orderBy("timestamp", Query.Direction.DESCENDING)
                                    .limit(1)
                                    .get()
                                    .addOnSuccessListener { msgSnapshot ->
                                        val messageDoc = msgSnapshot.documents.firstOrNull()
                                        val msgText = messageDoc?.getString("text") ?: "No messages yet"
                                        val msgTime = messageDoc?.getTimestamp("timestamp")?.toDate()?.time ?: 0L

                                        lastMessages[user.uid] = Pair(msgText, msgTime)
                                        filterUsers(searchBar.text.toString())
                                    }
                            }
                        }
                        filterUsers(searchBar.text.toString())
                    }
            }
    }


    private fun generateChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_$uid2" else "${uid2}_$uid1"
    }

    private fun filterUsers(query: String) {
        val lowerQuery = query.lowercase()
        filteredUsers.clear()

        filteredUsers.addAll(
            users.filter { it.name.lowercase().contains(lowerQuery) }
                .sortedByDescending { lastMessages[it.uid]?.second ?: 0L }
        )

        adapter.setLastMessages(lastMessages)
        adapter.notifyDataSetChanged()
    }
}
