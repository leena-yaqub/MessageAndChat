package com.example.messageandchat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.messageandchat.R
import com.example.messageandchat.models.User
import java.text.SimpleDateFormat
import java.util.*

class UserListAdapter(
    private val usersList: List<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserListAdapter.UserListViewHolder>() {

    class UserListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        val ivUserAvatar: ImageView = itemView.findViewById(R.id.ivUserAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return UserListViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserListViewHolder, position: Int) {
        val user = usersList[position]

        holder.tvUserName.text = user.name
        holder.tvLastMessage.text = "Tap to start chatting"

        if (user.isOnline) {
            holder.tvTimestamp.text = "Online"
        } else {
            val lastSeen = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                .format(Date(user.lastSeen))
            holder.tvTimestamp.text = "Last seen: $lastSeen"
        }

        holder.itemView.setOnClickListener {
            onUserClick(user)
        }
    }

    override fun getItemCount(): Int = usersList.size
}