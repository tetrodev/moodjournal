package com.example.moodjournal

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.moodjournal.data.User
import com.example.moodjournal.data.UserRepository

class LoginActivity : AppCompatActivity() {

    private lateinit var etLogin: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var prefs: SharedPreferences
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        etLogin = findViewById(R.id.etLogin)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)

        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        userRepository = UserRepository(this)

        if (prefs.getBoolean("is_logged_in", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnLogin.setOnClickListener {
            val login = etLogin.text.toString().trim()
            val password = etPassword.text.toString().trim()

            when {
                login.isEmpty() || password.isEmpty() -> {
                    Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show()
                }
                userRepository.checkLogin(login, password) -> {
                    prefs.edit().putBoolean("is_logged_in", true).apply()
                    prefs.edit().putString("current_user", login).apply()
                    Toast.makeText(this, "✅ Добро пожаловать, $login!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                else -> {
                    Toast.makeText(this, "❌ Неверный логин или пароль!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tvRegister.setOnClickListener {
            showRegistrationDialog()
        }
    }

    private fun showRegistrationDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("📝 Регистрация")

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
        }

        val etRegLogin = EditText(this).apply {
            hint = "Логин"
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 20 }
        }
        rootLayout.addView(etRegLogin)

        val etRegName = EditText(this).apply {
            hint = "Имя (необязательно)"
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 20 }
        }
        rootLayout.addView(etRegName)

        val etRegPassword = EditText(this).apply {
            hint = "Пароль"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 20 }
        }
        rootLayout.addView(etRegPassword)

        val etRegConfirm = EditText(this).apply {
            hint = "Подтвердите пароль"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        rootLayout.addView(etRegConfirm)

        builder.setView(rootLayout)

        builder.setPositiveButton("Зарегистрироваться") { _, _ ->
            val login = etRegLogin.text.toString().trim()
            val password = etRegPassword.text.toString().trim()
            val confirm = etRegConfirm.text.toString().trim()
            val name = etRegName.text.toString().trim()

            when {
                login.isEmpty() || password.isEmpty() || confirm.isEmpty() -> {
                    Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show()
                }
                password != confirm -> {
                    Toast.makeText(this, "Пароли не совпадают!", Toast.LENGTH_SHORT).show()
                }
                password.length < 4 -> {
                    Toast.makeText(this, "Пароль должен быть не менее 4 символов!", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val newUser = User(login, password, name.ifEmpty { login })
                    val success = userRepository.registerUser(newUser)
                    if (success) {
                        Toast.makeText(this, "✅ Регистрация успешна! Теперь войдите.", Toast.LENGTH_LONG).show()
                        etLogin.setText(login)
                        etPassword.setText(password)
                    } else {
                        Toast.makeText(this, "❌ Пользователь с таким логином уже существует!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        builder.setNegativeButton("Отмена", null)
        builder.show()
    }
}