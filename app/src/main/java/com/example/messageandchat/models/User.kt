package com.example.messageandchat.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L
) : Parcelable
