package com.example.nurture_nest

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nurture_nest.adapters.MessageAdapter
import com.example.nurture_nest.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var tvChatName: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var adapter: MessageAdapter

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val messages = mutableListOf<Message>()
    private var chatId: String? = null
    private var receiverId: String? = null
    private var receiverName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerMessages = findViewById(R.id.recyclerMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        tvChatName = findViewById(R.id.tvChatName)
        btnBack = findViewById(R.id.btnBack)

        receiverId = intent.getStringExtra("receiverId")
        receiverName = intent.getStringExtra("chatName")
        val currentUserId = auth.currentUser?.uid ?: return

        tvChatName.text = receiverName ?: "Chat"
        btnBack.setOnClickListener { finish() }

        recyclerMessages.layoutManager = LinearLayoutManager(this)
        adapter = MessageAdapter(messages)
        recyclerMessages.adapter = adapter

        // ✅ Use consistent chatId format (NO "chat_" prefix)
        chatId = if (currentUserId < (receiverId ?: "")) {
            "${currentUserId}_${receiverId}"
        } else {
            "${receiverId}_${currentUserId}"
        }

        listenForMessages()

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty() && receiverId != null) {
                sendMessage(currentUserId, receiverId!!, text)
                etMessage.text.clear()
            }
        }
    }

    private fun listenForMessages() {
        db.collection("chats").document(chatId!!)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                messages.clear()
                for (doc in snapshots.documents) {
                    val message = doc.toObject(Message::class.java)
                    if (message != null) messages.add(message)
                }
                adapter.notifyDataSetChanged()
                recyclerMessages.scrollToPosition(messages.size - 1)
            }
    }

    private fun sendMessage(senderId: String, receiverId: String, text: String) {
        val timestamp = Timestamp.now()

        val message = hashMapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "text" to text,
            "timestamp" to timestamp
        )

        val chatRef = db.collection("chats").document(chatId!!)

        // 1️⃣ Add message to messages subcollection
        chatRef.collection("messages").add(message)
            .addOnSuccessListener {
                // 2️⃣ Update summary info in parent chat doc
                val chatSummary = hashMapOf(
                    "participants" to listOf(senderId, receiverId),
                    "lastMessage" to text,
                    "lastMessageTime" to timestamp.toDate().time // store as Long
                )

                chatRef.set(chatSummary, SetOptions.merge())
            }
    }
}
