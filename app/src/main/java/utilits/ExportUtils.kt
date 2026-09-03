package com.example.moodjournal.utils

import android.content.Context
import android.widget.Toast
import com.example.moodjournal.data.MoodEntry
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExportUtils {

    fun exportToFile(context: Context, entries: List<MoodEntry>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val fileName = "mood_journal_${dateFormat.format(Date())}.txt"
        val file = File(context.getExternalFilesDir(null), fileName)

        try {
            FileOutputStream(file).use { outputStream ->
                outputStream.write("📖 Дневник настроения - Экспорт\n".toByteArray())
                outputStream.write("=".repeat(50).toByteArray())
                outputStream.write("\n\n".toByteArray())

                if (entries.isEmpty()) {
                    outputStream.write("Нет записей".toByteArray())
                } else {
                    entries.reversed().forEach { entry ->
                        outputStream.write("📅 Дата: ${entry.date}\n".toByteArray())
                        outputStream.write("😊 Настроение: ${entry.moodEmoji} ${entry.moodLabel}\n".toByteArray())
                        outputStream.write("📝 Заметка: ${entry.note.ifEmpty { "Нет заметки" }}\n".toByteArray())
                        if (entry.tags.isNotEmpty()) {
                            outputStream.write("🏷️ Теги: ${entry.tags.joinToString(", ")}\n".toByteArray())
                        }
                        outputStream.write("-".repeat(30).toByteArray())
                        outputStream.write("\n".toByteArray())
                    }
                }
            }
            return "✅ Экспорт завершён: $fileName"
        } catch (e: Exception) {
            return "❌ Ошибка: ${e.message}"
        }
    }

    fun getTagStatistics(entries: List<MoodEntry>): Map<String, List<MoodEntry>> {
        val tagMap = mutableMapOf<String, MutableList<MoodEntry>>()
        entries.forEach { entry ->
            entry.tags.forEach { tag ->
                tagMap.getOrPut(tag) { mutableListOf() }.add(entry)
            }
        }
        return tagMap
    }

    fun getTrendData(entries: List<MoodEntry>): List<Pair<String, Double>> {
        val trendMap = mutableMapOf<String, MutableList<Int>>()
        entries.forEach { entry ->
            trendMap.getOrPut(entry.date) { mutableListOf() }.add(entry.moodValue)
        }
        return trendMap.map { (date, values) ->
            date to values.average()
        }.sortedBy { it.first }
    }
}
