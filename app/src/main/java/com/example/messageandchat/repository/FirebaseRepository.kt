package com.example.messageandchat.repository

import com.example.messageandchat.models.Message
import com.example.messageandchat.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class FirebaseRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val messaging: FirebaseMessaging = FirebaseMessaging.getInstance()

    fun registerUser(email: String, password: String, name: String, callback: (Boolean, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) saveUserToFirestore(user.uid, name, email, callback)
                    else callback(false, "Failed to get user")
                } else {
                    callback(false, task.exception?.message ?: "Registration failed")
                }
            }
    }

    fun loginUser(email: String, password: String, callback: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) callback(true, "Login successful")
                else callback(false, task.exception?.message ?: "Login failed")
            }
    }

    private fun saveUserToFirestore(uid: String, name: String, email: String, callback: (Boolean, String) -> Unit) {
        val user = hashMapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener { callback(true, "User saved successfully") }
            .addOnFailureListener { e -> callback(false, e.message ?: "Failed to save user") }
    }

    fun getFcmToken(callback: (String?) -> Unit) {
        messaging.token.addOnCompleteListener { task: Task<String> ->
            callback(if (task.isSuccessful) task.result else null)
        }
    }

    fun sendMessage(chatId: String, message: Message, callback: (Boolean) -> Unit) {
        val messageMap = hashMapOf(
            "senderId" to message.senderId,
            "receiverId" to message.receiverId,
            "message" to message.content,  // IMPORTANT: use content
            "timestamp" to message.timestamp,
            "type" to message.type
        )

        firestore.collection("chats").document(chatId)
            .collection("messages")
            .add(messageMap)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun getUsers(currentUserUid: String, callback: (List<User>) -> Unit) {
        firestore.collection("users")
            .whereNotEqualTo("uid", currentUserUid)
            .get()
            .addOnSuccessListener { docs ->
                val users = docs.map { doc ->
                    User(
                        uid = doc.getString("uid") ?: "",
                        name = doc.getString("name") ?: "",
                        email = doc.getString("email") ?: ""
                    )
                }
                callback(users)
            }
            .addOnFailureListener { callback(emptyList()) }
    }
}
