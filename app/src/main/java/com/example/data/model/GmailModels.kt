package com.example.data.model

data class GmailEmailItem(
    val id: String,
    val sender: String,
    val senderEmail: String,
    val subject: String,
    val snippet: String,
    val date: String,
    val isRead: Boolean = true
)
