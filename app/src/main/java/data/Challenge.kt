package com.example.moodjournal.data

import android.content.Context
import android.content.SharedPreferences
import java.util.*

data class Challenge(
    val id: Int,
    val title: String,
    val description: String,
    val category: String
)

class ChallengeRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("challenges", Context.MODE_PRIVATE)

    private val challenges = listOf(
        Challenge(1, "Скажи себе комплимент", "Скажите вслух 3 приятных слова о себе", "Позитив"),
        Challenge(2, "Улыбнись незнакомцу", "Улыбнитесь прохожему или коллеге", "Социальное"),
        Challenge(3, "Сделай 10 приседаний", "Выполните 10 приседаний прямо сейчас", "Спорт"),
        Challenge(4, "Запиши 3 хорошие вещи", "Напишите 3 вещи, за которые вы благодарны сегодня", "Осознанность"),
        Challenge(5, "Выпей стакан воды", "Выпейте стакан воды, пока не забыли", "Здоровье"),
        Challenge(6, "Глубоко подыши", "Сделайте 5 глубоких вдохов и выдохов", "Релаксация"),
        Challenge(7, "Скажи 'спасибо'", "Поблагодарите кого-то за что-то", "Социальное"),
        Challenge(8, "Почитай 5 минут", "Прочитайте книгу или статью 5 минут", "Развитие"),
        Challenge(9, "Сделай зарядку", "Выполните утреннюю зарядку 5 минут", "Спорт"),
        Challenge(10, "Помечтай", "Представьте свою идеальную жизнь 2 минуты", "Осознанность"),
        Challenge(11, "Обними кого-то", "Обнимите близкого человека", "Социальное"),
        Challenge(12, "Сходи на прогулку", "Выйдите на улицу на 10 минут", "Спорт"),
        Challenge(13, "Сделай доброе дело", "Сделайте что-то хорошее для другого", "Социальное"),
        Challenge(14, "Посмотри в окно", "Посмотрите в окно и найдите 5 красивых вещей", "Осознанность"),
        Challenge(15, "Напиши письмо", "Напишите письмо другу или себе", "Осознанность")
    )

    fun getDailyChallenge(): Challenge {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = dayOfYear % challenges.size
        return challenges[index]
    }

    fun getCompletedIds(): Set<String> {
        return prefs.getStringSet("completed_challenges", emptySet()) ?: emptySet()
    }

    fun isChallengeCompleted(date: String): Boolean {
        val completed = getCompletedIds()
        return completed.contains(date)
    }

    fun completeChallenge(date: String) {
        val completed = getCompletedIds().toMutableSet()
        completed.add(date)
        prefs.edit().putStringSet("completed_challenges", completed).apply()
    }

    fun getAllChallenges(): List<Challenge> = challenges
}