package com.example.messageandchat.repositories

import com.example.messageandchat.models.Chat
import com.example.messageandchat.models.Message
import com.example.messageandchat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val usersCollection = firestore.collection("users")
    private val chatsCollection = firestore.collection("chats")

    suspend fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun getUserById(userId: String): User? = try {
        val doc = usersCollection.document(userId).get().await()
        doc.toObject(User::class.java)?.copy(uid = userId)
    } catch (e: Exception) {
        null
    }

    suspend fun getAllUsers(): List<User> = try {
        usersCollection.get().await().toObjects(User::class.java)
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun getUserChats(userId: String): List<Chat> = try {
        chatsCollection.whereArrayContains("participants", userId).get().await().toObjects(Chat::class.java)
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun getMessages(chatId: String): List<Message> = try {
        chatsCollection.document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .get()
            .await()
            .toObjects(Message::class.java)
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun sendMessage(chatId: String, message: Message) {
        chatsCollection.document(chatId).collection("messages").add(message).await()
        val updates = mapOf("lastMessage" to message, "lastMessageTime" to message.timestamp)
        chatsCollection.document(chatId).update(updates).await()
    }
}
