package com.example.moodjournal.ui.fragments

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.moodjournal.R
import com.example.moodjournal.data.MoodEntry
import com.example.moodjournal.data.MoodRepository
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private lateinit var repository: MoodRepository
    private lateinit var monthText: TextView
    private lateinit var calendarContainer: LinearLayout
    private lateinit var entriesCountText: TextView
    private lateinit var prevMonthBtn: TextView
    private lateinit var nextMonthBtn: TextView

    private var currentCalendar = Calendar.getInstance()
    private var entries: List<MoodEntry> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = MoodRepository(requireContext())

        monthText = view.findViewById(R.id.monthText)
        calendarContainer = view.findViewById(R.id.calendarContainer)
        entriesCountText = view.findViewById(R.id.entriesCountText)
        prevMonthBtn = view.findViewById(R.id.prevMonthBtn)
        nextMonthBtn = view.findViewById(R.id.nextMonthBtn)

        prevMonthBtn.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            loadCalendar()
        }

        nextMonthBtn.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            loadCalendar()
        }

        loadCalendar()
    }

    override fun onResume() {
        super.onResume()
        loadCalendar()
    }

    private fun loadCalendar() {
        try {
            entries = repository.getAllEntries()


            val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("ru"))
            val monthStr = monthFormat.format(currentCalendar.time)
            monthText.text = monthStr.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

            val year = currentCalendar.get(Calendar.YEAR)
            val month = currentCalendar.get(Calendar.MONTH)
            val monthEntries = entries.filter {
                val parts = it.date.split("-")
                parts.size == 3 && parts[1].toInt() - 1 == month && parts[0].toInt() == year
            }
            entriesCountText.text = "📝 ${monthEntries.size} записей"

            calendarContainer.removeAllViews()

            addWeekDays()
            addDays(year, month)

        } catch (e: Exception) {
            e.printStackTrace()
            entriesCountText.text = "❌ Ошибка загрузки календаря"
        }
    }

    private fun addWeekDays() {
        val weekDays = arrayOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        val row = LinearLayout(requireContext())
        row.orientation = LinearLayout.HORIZONTAL
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        row.setPadding(0, 8, 0, 8)

        for (day in weekDays) {
            val tv = TextView(requireContext())
            tv.text = day
            tv.textSize = 14f
            tv.gravity = android.view.Gravity.CENTER
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            tv.setTypeface(null, android.graphics.Typeface.BOLD)
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            tv.layoutParams = params
            row.addView(tv)
        }

        calendarContainer.addView(row)
    }

    private fun addDays(year: Int, month: Int) {

        val firstDayCal = Calendar.getInstance()
        firstDayCal.set(year, month, 1)
        var firstDayOfWeek = firstDayCal.get(Calendar.DAY_OF_WEEK)
        firstDayOfWeek = if (firstDayOfWeek == Calendar.SUNDAY) 7 else firstDayOfWeek - 1

        val daysInMonth = getDaysInMonth(year, month)
        val today = Calendar.getInstance()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.time)

        var dayCount = 1
        var row = createRow()


        for (i in 1 until firstDayOfWeek) {
            val emptyView = createEmptyDayView()
            row.addView(emptyView)
        }


        while (dayCount <= daysInMonth) {
            val dateStr = String.format("%04d-%02d-%02d", year, month + 1, dayCount)
            val entry = entries.find { it.date == dateStr }
            val isToday = dateStr == todayStr

            val dayView = createDayView(dayCount, dateStr, entry, isToday)
            row.addView(dayView)
            dayCount++

            if (row.childCount == 7) {
                calendarContainer.addView(row)
                row = createRow()
            }
        }

        while (row.childCount < 7) {
            val emptyView = createEmptyDayView()
            row.addView(emptyView)
        }

        if (row.childCount > 0) {
            calendarContainer.addView(row)
        }
    }

    private fun getDaysInMonth(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun createRow(): LinearLayout {
        val row = LinearLayout(requireContext())
        row.orientation = LinearLayout.HORIZONTAL
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        row.setPadding(0, 4, 0, 4)
        return row
    }

    private fun createEmptyDayView(): View {
        val view = View(requireContext())
        val params = LinearLayout.LayoutParams(0, 80, 1f)
        view.layoutParams = params
        return view
    }

    private fun createDayView(day: Int, dateStr: String, entry: MoodEntry?, isToday: Boolean): View {
        val container = LinearLayout(requireContext())
        container.orientation = LinearLayout.VERTICAL
        container.gravity = android.view.Gravity.CENTER
        container.setPadding(4, 6, 4, 6)

        val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        container.layoutParams = params

        val background = GradientDrawable()
        background.cornerRadius = 12f

        try {
            if (entry != null) {
                background.setColor(ContextCompat.getColor(requireContext(), R.color.mood_selected))
                background.setStroke(2, ContextCompat.getColor(requireContext(), R.color.primary))
                container.isClickable = true
                container.isFocusable = true

                container.setOnClickListener {
                    showDayDetails(entry)
                }
            } else {
                if (isToday) {
                    background.setColor(ContextCompat.getColor(requireContext(), R.color.primary_light))
                    background.setStroke(1, ContextCompat.getColor(requireContext(), R.color.primary))
                } else {
                    background.setColor(Color.TRANSPARENT)
                    background.setStroke(1, Color.parseColor("#EEEEEE"))
                }
                container.isClickable = false
            }
        } catch (e: Exception) {
            background.setColor(Color.TRANSPARENT)
            background.setStroke(1, Color.parseColor("#EEEEEE"))
        }

        container.background = background


        val dayText = TextView(requireContext())
        dayText.text = day.toString()
        dayText.textSize = 16f
        dayText.gravity = android.view.Gravity.CENTER
        dayText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        if (isToday) {
            dayText.setTypeface(null, android.graphics.Typeface.BOLD)
        }
        container.addView(dayText)


        if (entry != null) {
            val emojiText = TextView(requireContext())
            emojiText.text = entry.moodEmoji
            emojiText.textSize = 18f
            emojiText.gravity = android.view.Gravity.CENTER
            container.addView(emojiText)
        }

        return container
    }

    private fun showDayDetails(entry: MoodEntry) {
        try {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("📅 ${entry.date}")

            val message = buildString {
                appendLine("😊 ${entry.moodEmoji} ${entry.moodLabel}")
                appendLine()
                if (entry.title.isNotEmpty()) {
                    appendLine("📌 ${entry.title}")
                    appendLine()
                }
                if (entry.note.isNotEmpty()) {
                    appendLine("📝 ${entry.note}")
                    appendLine()
                }
                if (entry.tags.isNotEmpty()) {
                    appendLine("🏷️ Теги: ${entry.tags.joinToString(", ")}")
                }
                if (entry.title.isEmpty() && entry.note.isEmpty()) {
                    appendLine("(без заметки)")
                }
            }

            builder.setMessage(message)
            builder.setPositiveButton("Закрыть", null)
            builder.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}