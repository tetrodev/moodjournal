package com.example.moodjournal.ui.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.moodjournal.R
import com.example.moodjournal.services.ReminderService
import java.util.*

class SettingsFragment : Fragment() {

    private lateinit var prefs: SharedPreferences
    private lateinit var reminderSwitch: Switch
    private lateinit var themeSwitch: Switch
    private lateinit var reminderStatusText: TextView
    private lateinit var testButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        reminderSwitch = view.findViewById(R.id.reminderSwitch)
        themeSwitch = view.findViewById(R.id.themeSwitch)
        reminderStatusText = view.findViewById(R.id.reminderStatusText)
        testButton = view.findViewById(R.id.testNotificationButton)

        val isReminderOn = prefs.getBoolean("reminder_enabled", true)
        reminderSwitch.isChecked = isReminderOn
        updateReminderStatus(isReminderOn)

        reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableReminder()
            } else {
                disableReminder()
            }
            updateReminderStatus(isChecked)
        }

        testButton.setOnClickListener {
            if (reminderSwitch.isChecked) {
                sendTestNotification()
            } else {
                Toast.makeText(requireContext(), "Сначала включите напоминалку", Toast.LENGTH_SHORT).show()
            }
        }

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        themeSwitch.isChecked = currentNightMode == Configuration.UI_MODE_NIGHT_YES

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            activity?.recreate()
        }
    }

    private fun updateReminderStatus(isOn: Boolean) {
        reminderStatusText.text = if (isOn) {
            "🔔 Напоминания включены (21:00)"
        } else {
            "🔕 Напоминания отключены"
        }
    }

    private fun enableReminder() {
        prefs.edit().putBoolean("reminder_enabled", true).apply()
        scheduleReminder()
        Toast.makeText(requireContext(), "🔔 Напоминания включены", Toast.LENGTH_SHORT).show()
    }

    private fun disableReminder() {
        prefs.edit().putBoolean("reminder_enabled", false).apply()
        cancelReminder()
        Toast.makeText(requireContext(), "🔕 Напоминания отключены", Toast.LENGTH_SHORT).show()
    }

    private fun scheduleReminder() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), ReminderService::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancelReminder() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), ReminderService::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun sendTestNotification() {
        val intent = Intent(requireContext(), ReminderService::class.java)
        ReminderService().onReceive(requireContext(), intent)
        Toast.makeText(requireContext(), "🔔 Тестовое уведомление отправлено!", Toast.LENGTH_SHORT).show()
    }
}
