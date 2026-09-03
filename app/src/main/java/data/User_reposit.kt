package com.example.moodjournal.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("users", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun registerUser(user: User): Boolean {
        if (getUser(user.login) != null) {
            return false
        }

        val users = getAllUsers().toMutableList()
        users.add(user)

        val json = gson.toJson(users)
        prefs.edit().putString("users_list", json).apply()

        return true
    }

    fun getUser(login: String): User? {
        return getAllUsers().find { it.login.equals(login, ignoreCase = true) }
    }

    fun checkLogin(login: String, password: String): Boolean {
        val user = getUser(login)
        return user != null && user.password == password
    }

    private fun getAllUsers(): List<User> {
        val json = prefs.getString("users_list", null)
        if (json == null) {
            val defaultUsers = listOf(
                User("admin", "12345", "Администратор"),
                User("user", "12345", "Пользователь")
            )
            saveAllUsers(defaultUsers)
            return defaultUsers
        }

        val type = object : TypeToken<List<User>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun saveAllUsers(users: List<User>) {
        val json = gson.toJson(users)
        prefs.edit().putString("users_list", json).apply()
    }
}