package com.example.moodjournal.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class MoodRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("MoodJournal", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun saveEntry(entry: MoodEntry) {
        val json = gson.toJson(entry)
        prefs.edit().putString("mood_${entry.date}", json).apply()
    }

    fun getEntry(date: String): MoodEntry? {
        val json = prefs.getString("mood_$date", null) ?: return null
        return gson.fromJson(json, MoodEntry::class.java)
    }

    fun getAllEntries(): List<MoodEntry> {
        val entries = mutableListOf<MoodEntry>()
        val allKeys = prefs.all.keys

        for (key in allKeys) {
            if (key.startsWith("mood_")) {
                val json = prefs.getString(key, null)
                json?.let {
                    val entry = gson.fromJson(it, MoodEntry::class.java)
                    entries.add(entry)
                }
            }
        }

        return entries.sortedBy { it.date }
    }

    fun getWeekEntries(): List<MoodEntry> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val weekStart = dateFormat.format(calendar.time)
        return getAllEntries().filter { it.date >= weekStart }
    }

    fun getStatistics(): Statistics {
        val all = getAllEntries()
        val week = getWeekEntries()
        val avgWeek = if (week.isNotEmpty()) week.map { it.moodValue }.average() else 0.0
        val bestMood = all.maxByOrNull { it.moodValue }

        return Statistics(
            totalEntries = all.size,
            weekCount = week.size,
            avgWeek = avgWeek,
            bestMood = bestMood
        )
    }

    data class Statistics(
        val totalEntries: Int,
        val weekCount: Int,
        val avgWeek: Double,
        val bestMood: MoodEntry?
    )
}
