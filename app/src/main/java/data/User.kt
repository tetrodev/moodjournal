package com.example.moodjournal.data

data class User(
    val login: String,
    val password: String,
    val name: String = "",
    val createdAt: Long = System.currentTimeMillis()
)