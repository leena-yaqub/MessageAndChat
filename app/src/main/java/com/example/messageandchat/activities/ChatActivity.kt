package com.example.messageandchat.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messageandchat.R
import com.example.messageandchat.adapters.MessageAdapter
import com.example.messageandchat.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var receiverUid: String = ""
    private var receiverName: String = ""
    private var currentUserUid: String = ""
    private var chatId: String = ""

    private val messagesList = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        initializeViews()
        initializeFirebase()
        getIntentData()
        setupActionBar()
        setupRecyclerView()
        setupClickListeners()
        generateChatId()
        createChatDocument()
        loadMessages()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewMessages)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserUid = auth.currentUser?.uid ?: ""
    }

    private fun getIntentData() {
        receiverUid = intent.getStringExtra("receiver_uid") ?: ""
        receiverName = intent.getStringExtra("receiver_name") ?: ""
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            title = receiverName
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messagesList, currentUserUid)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = messageAdapter
        }
    }

    private fun setupClickListeners() {
        buttonSend.setOnClickListener {
            val messageText = editTextMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                editTextMessage.text.clear()
            }
        }
    }

    private fun generateChatId() {
        chatId = if (currentUserUid < receiverUid) "${currentUserUid}_${receiverUid}" else "${receiverUid}_${currentUserUid}"
    }

    private fun createChatDocument() {
        val chatData = hashMapOf(
            "chatId" to chatId,
            "participants" to listOf(currentUserUid, receiverUid),
            "createdAt" to System.currentTimeMillis(),
            "lastMessage" to "",
            "lastMessageTime" to 0L
        )

        firestore.collection("chats").document(chatId)
            .set(chatData)
            .addOnSuccessListener {
                // Chat document created/updated successfully
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error creating chat: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendMessage(messageText: String) {
        val timestamp = System.currentTimeMillis()
        val messageId = firestore.collection("chats").document(chatId)
            .collection("messages").document().id

        val message = hashMapOf(
            "messageId" to messageId,
            "senderId" to currentUserUid,
            "receiverId" to receiverUid,
            "content" to messageText,
            "timestamp" to timestamp,
            "type" to "text",
            "isRead" to false
        )

        // Add message to subcollection
        firestore.collection("chats").document(chatId)
            .collection("messages")
            .document(messageId)
            .set(message)
            .addOnSuccessListener {
                // Update chat document with last message info
                updateChatLastMessage(messageText, timestamp)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateChatLastMessage(messageText: String, timestamp: Long) {
        val updates = hashMapOf<String, Any>(
            "lastMessage" to messageText,
            "lastMessageTime" to timestamp
        )

        firestore.collection("chats").document(chatId)
            .update(updates)
            .addOnFailureListener { e ->
                // Handle update failure if needed
            }
    }

    private fun loadMessages() {
        firestore.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { documents, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading messages: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                messagesList.clear()
                documents?.forEach { document ->
                    val message = Message(
                        messageId = document.getString("messageId") ?: "",
                        senderId = document.getString("senderId") ?: "",
                        receiverId = document.getString("receiverId") ?: "",
                        content = document.getString("content") ?: "",
                        timestamp = document.getLong("timestamp") ?: 0L,
                        type = document.getString("type") ?: "text",
                        isRead = document.getBoolean("isRead") ?: false
                    )
                    messagesList.add(message)
                }
                messageAdapter.notifyDataSetChanged()
                if (messagesList.isNotEmpty()) {
                    recyclerView.scrollToPosition(messagesList.size - 1)
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        // Update user status when leaving chat
        if (currentUserUid.isNotEmpty()) {
            firestore.collection("users").document(currentUserUid)
                .update("lastSeen", System.currentTimeMillis())
        }
    }
}
