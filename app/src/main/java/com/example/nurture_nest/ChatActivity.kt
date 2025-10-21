package com.example.nurture_nest

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nurture_nest.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

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

        // ✅ Create consistent chatId
        chatId = if (currentUserId < receiverId!!) {
            "chat_${currentUserId}_${receiverId}"
        } else {
            "chat_${receiverId}_${currentUserId}"
        }

        // ✅ Listen to Firestore for messages
        listenForMessages()

        // ✅ Send button logic
        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
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
                if (error != null) return@addSnapshotListener

                messages.clear()
                for (doc in snapshots!!) {
                    val message = doc.toObject(Message::class.java)
                    messages.add(message)
                }
                adapter.notifyDataSetChanged()
                recyclerMessages.scrollToPosition(messages.size - 1)
            }
    }

    private fun sendMessage(senderId: String, receiverId: String, text: String) {
        val message = hashMapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "text" to text,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        db.collection("chats").document(chatId!!)
            .collection("messages")
            .add(message)
    }
}
