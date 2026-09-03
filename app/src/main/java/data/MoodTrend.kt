package com.example.moodjournal.data

data class MoodTrend(
    val date: String,
    val moodValue: Int,
    val moodEmoji: String,
    val dayName: String
)

data class TagStats(
    val tag: String,
    val averageMood: Double,
    val count: Int
)

