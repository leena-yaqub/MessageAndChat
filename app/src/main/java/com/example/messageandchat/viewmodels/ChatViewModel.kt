package com.example.messageandchat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messageandchat.models.Message
import com.example.messageandchat.models.User
import com.example.messageandchat.repositories.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _otherUser = MutableStateFlow<User?>(null)
    val otherUser: StateFlow<User?> = _otherUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadChat(chatId: String, otherUserId: String) {
        loadMessages(chatId)
        loadUserData(otherUserId)
    }

    private fun loadMessages(chatId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _messages.value = repository.getMessages(chatId)
            } catch (e: Exception) {
                // Handle exception
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadUserData(otherUserId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = repository.getCurrentUserId()
                if (currentUserId != null) {
                    _currentUser.value = repository.getUserById(currentUserId)
                }
                _otherUser.value = repository.getUserById(otherUserId)
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }

    fun sendMessage(chatId: String, content: String, receiverId: String) {
        viewModelScope.launch {
            try {
                val senderId = repository.getCurrentUserId()
                if (senderId != null) {
                    val message = Message(
                        messageId = "",
                        senderId = senderId,
                        receiverId = receiverId,
                        content = content,            // <-- content
                        timestamp = System.currentTimeMillis(),
                        type = "text"
                    )
                    repository.sendMessage(chatId, message)
                    loadMessages(chatId)
                }
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }
}
