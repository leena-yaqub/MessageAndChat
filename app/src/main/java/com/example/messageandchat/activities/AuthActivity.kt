package com.example.messageandchat.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.messageandchat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnLogin: Button
    private lateinit var tvToggleAuth: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    private var isLoginMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Check if already logged in
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            navigateToChatList()
            return
        }

        initializeViews()
        initializeFirebase()
        setupClickListeners()
    }

    private fun initializeViews() {
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        btnLogin = findViewById(R.id.btnLogin)
        tvToggleAuth = findViewById(R.id.tvToggleAuth)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
    }

    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            if (isLoginMode) {
                loginUser()
            } else {
                registerUser()
            }
        }

        btnLogin.setOnClickListener {
            loginUser()
        }

        tvToggleAuth.setOnClickListener {
            toggleAuthMode()
        }
    }

    private fun toggleAuthMode() {
        isLoginMode = !isLoginMode
        if (isLoginMode) {
            btnRegister.text = "Login"
            btnLogin.visibility = View.GONE
            tvToggleAuth.text = "Don't have an account? Register"
            etName.visibility = View.GONE
        } else {
            btnRegister.text = "Register"
            btnLogin.visibility = View.VISIBLE
            tvToggleAuth.text = "Already have an account? Login"
            etName.visibility = View.VISIBLE
        }
    }

    private fun registerUser() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""
                val user = hashMapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "isOnline" to true,
                    "lastSeen" to System.currentTimeMillis()
                )

                firestore.collection("users").document(uid)
                    .set(user)
                    .addOnSuccessListener {
                        showLoading(false)
                        saveLoginState(true)
                        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                        navigateToChatList()
                    }
                    .addOnFailureListener { e ->
                        showLoading(false)
                        Toast.makeText(this, "Error saving user: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email & Password required", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""
                // Update user online status
                firestore.collection("users").document(uid)
                    .update(mapOf(
                        "isOnline" to true,
                        "lastSeen" to System.currentTimeMillis()
                    ))

                showLoading(false)
                saveLoginState(true)
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                navigateToChatList()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !show
        btnLogin.isEnabled = !show
    }

    private fun saveLoginState(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean("is_logged_in", isLoggedIn).apply()
    }

    private fun navigateToChatList() {
        val intent = Intent(this, ChatListActivity::class.java)
        startActivity(intent)
        finish()
    }
}