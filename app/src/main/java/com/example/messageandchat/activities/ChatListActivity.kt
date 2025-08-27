package com.example.messageandchat.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messageandchat.R
import com.example.messageandchat.adapters.UserListAdapter
import com.example.messageandchat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbar: Toolbar
    private lateinit var userAdapter: UserListAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    private val usersList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        initializeViews()
        initializeFirebase()
        checkLoginStatus()
        setupRecyclerView()
        loadUsers()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewUsers)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Chat List"
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
    }

    private fun checkLoginStatus() {
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
        if (!isLoggedIn || auth.currentUser == null) {
            navigateToAuth()
            return
        }
    }

    private fun setupRecyclerView() {
        userAdapter = UserListAdapter(usersList) { user ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("receiver_uid", user.uid)
            intent.putExtra("receiver_name", user.name)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = userAdapter
    }

    private fun loadUsers() {
        val currentUserUid = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .whereNotEqualTo("uid", currentUserUid)
            .addSnapshotListener { documents, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading users: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                usersList.clear()
                documents?.forEach { document ->
                    val user = User(
                        uid = document.getString("uid") ?: "",
                        name = document.getString("name") ?: "",
                        email = document.getString("email") ?: "",
                        isOnline = document.getBoolean("isOnline") ?: false,
                        lastSeen = document.getLong("lastSeen") ?: 0L
                    )
                    usersList.add(user)
                }
                userAdapter.notifyDataSetChanged()

                if (usersList.isEmpty()) {
                    Toast.makeText(this, "No users found. Try registering another account.", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_list_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                loadUsers()
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid != null) {
            firestore.collection("users").document(currentUserUid)
                .update("isOnline", false)
        }

        auth.signOut()
        sharedPreferences.edit().putBoolean("is_logged_in", false).apply()
        navigateToAuth()
    }

    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }
}