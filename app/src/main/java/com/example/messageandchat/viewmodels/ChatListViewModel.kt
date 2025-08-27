package com.example.messageandchat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messageandchat.models.Chat
import com.example.messageandchat.models.User
import com.example.messageandchat.repositories.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        getAllUsers()
        getUserChats()
    }

    private fun getAllUsers() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val allUsers = repository.getAllUsers()
                _users.value = allUsers
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getUserChats() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val currentUserId = repository.getCurrentUserId()
                if (currentUserId != null) {
                    val userChats = repository.getUserChats(currentUserId)
                    _chats.value = userChats
                } else {
                    _errorMessage.value = "Current user ID not found"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getUserById(userId: String, onResult: (User?) -> Unit) {
        viewModelScope.launch {
            try {
                val user = repository.getUserById(userId)
                onResult(user)
            } catch (e: Exception) {
                _errorMessage.value = e.message
                onResult(null)
            }
        }
    }
}
