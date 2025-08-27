package com.example.messageandchat

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.messageandchat.activities.AuthActivity
import com.example.messageandchat.activities.ChatListActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        checkUserLoginStatus()
    }

    private fun checkUserLoginStatus() {
        val currentUser = auth.currentUser
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)

        if (currentUser != null && isLoggedIn) {
            navigateToChatList()
        } else {
            navigateToAuth()
        }
    }

    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToChatList() {
        val intent = Intent(this, ChatListActivity::class.java)
        startActivity(intent)
        finish()
    }
}