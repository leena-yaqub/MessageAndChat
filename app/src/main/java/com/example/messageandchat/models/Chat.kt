package com.example.messageandchat.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Chat(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: Message? = null,
    val lastMessageTime: Long = 0L,
    val unreadCount: Map<String, Int> = emptyMap()
) : Parcelable
