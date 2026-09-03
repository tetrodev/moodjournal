package com.example.moodjournal.data

data class MoodEntry(
    val date: String,
    val moodValue: Int,
    val moodEmoji: String,
    val moodLabel: String,
    val title: String = "",
    val note: String = "",
    val tags: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class Mood(val value: Int, val emoji: String, val label: String) {
        EXCELLENT(5, "😄", "Отлично"),
        GOOD(4, "🙂", "Хорошо"),
        NORMAL(3, "😐", "Нормально"),
        BAD(2, "😔", "Плохо"),
        TERRIBLE(1, "😢", "Ужасно");

        companion object {
            fun fromValue(value: Int): Mood? {
                return values().find { it.value == value }
            }
        }
    }
}
