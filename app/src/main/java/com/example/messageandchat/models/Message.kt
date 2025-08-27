package com.example.messageandchat.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",        // main text field
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "text",
    val isRead: Boolean = false
) : Parcelable
