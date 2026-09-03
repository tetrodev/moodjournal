package com.example.moodjournal.ui.fragments
import androidx.core.content.ContextCompat
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moodjournal.R
import com.example.moodjournal.data.MoodEntry
import com.example.moodjournal.data.MoodRepository
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private lateinit var repository: MoodRepository
    private lateinit var pieChart: PieChart
    private lateinit var lineChart: LineChart
    private lateinit var statsTextView: TextView
    private lateinit var historyRecycler: RecyclerView
    private lateinit var monthSpinner: Spinner
    private lateinit var shareButton: Button
    private lateinit var exportButton: Button

    private var allEntries: List<MoodEntry> = emptyList()
    private var currentMonth: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_prof, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = MoodRepository(requireContext())

        pieChart = view.findViewById(R.id.pieChart)
        lineChart = view.findViewById(R.id.lineChart)
        statsTextView = view.findViewById(R.id.statsTextView)
        historyRecycler = view.findViewById(R.id.historyRecycler)
        monthSpinner = view.findViewById(R.id.monthSpinner)
        shareButton = view.findViewById(R.id.shareButton)
        exportButton = view.findViewById(R.id.exportButton)

        setupMonthSpinner()
        loadData()

        shareButton.setOnClickListener { shareStats() }
        exportButton.setOnClickListener { exportToText() }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupMonthSpinner() {
        val months = listOf("📊 Всё время", "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            months
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter.setDropDownViewTheme(requireContext().theme)

        monthSpinner.adapter = adapter

        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (view is TextView) {
                    view.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                }
                currentMonth = position - 1
                loadData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadData() {
        allEntries = repository.getAllEntries()

        val filtered = if (currentMonth == -1) {
            allEntries
        } else {
            allEntries.filter {
                val month = it.date.split("-")[1].toInt()
                month == currentMonth + 1
            }
        }

        if (filtered.isEmpty()) {
            statsTextView.text = if (currentMonth == -1) {
                "📭 Нет данных за всё время"
            } else {
                "📭 Нет данных за выбранный месяц"
            }
            pieChart.visibility = View.GONE
            lineChart.visibility = View.GONE
            return
        }

        pieChart.visibility = View.VISIBLE
        lineChart.visibility = View.VISIBLE

        val avgMood = filtered.map { it.moodValue }.average()
        val bestMood = filtered.maxByOrNull { it.moodValue }
        val worstMood = filtered.minByOrNull { it.moodValue }
        val totalEntries = filtered.size

        val periodText = if (currentMonth == -1) "за всё время" else "за выбранный месяц"
        statsTextView.text = buildString {
            appendLine("📊 Ваша статистика $periodText")
            appendLine()
            appendLine("📝 Всего записей: $totalEntries")
            appendLine("⭐ Средний балл: ${String.format("%.1f", avgMood)} / 5")
            appendLine("🏆 Лучший день: ${bestMood?.moodEmoji} ${bestMood?.moodLabel} (${bestMood?.date})")
            appendLine("📉 Худший день: ${worstMood?.moodEmoji} ${worstMood?.moodLabel} (${worstMood?.date})")
        }

        setupPieChart(filtered)
        setupLineChart(filtered)

        val adapter = HistoryAdapter()
        historyRecycler.layoutManager = LinearLayoutManager(requireContext())
        historyRecycler.adapter = adapter
        adapter.submitList(filtered.reversed().take(10))
    }

    private fun setupPieChart(entries: List<MoodEntry>) {
        if (entries.isEmpty()) return

        val moodCounts = mutableMapOf<Int, Int>()
        for (entry in entries) {
            moodCounts[entry.moodValue] = moodCounts.getOrDefault(entry.moodValue, 0) + 1
        }

        val pieEntries = mutableListOf<PieEntry>()

        for ((value, count) in moodCounts) {
            val mood = MoodEntry.Mood.fromValue(value)
            val label = mood?.label ?: "Неизвестно"
            pieEntries.add(PieEntry(count.toFloat(), label))
        }

        if (pieEntries.isEmpty()) return

        val dataSet = PieDataSet(pieEntries, "Настроения")
        dataSet.colors = listOf(
            resources.getColor(R.color.mood_excellent, null),
            resources.getColor(R.color.mood_good, null),
            resources.getColor(R.color.mood_normal, null),
            resources.getColor(R.color.mood_bad, null),
            resources.getColor(R.color.mood_terrible, null)
        )
        dataSet.valueTextColor = resources.getColor(R.color.text_primary, null)
        dataSet.valueTextSize = 14f
        dataSet.setDrawValues(true)
        dataSet.sliceSpace = 3f

        val pieData = PieData(dataSet)
        pieChart.data = pieData

        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(false)
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 40f
        pieChart.setHoleColor(resources.getColor(R.color.background, null))
        pieChart.legend.isEnabled = true
        pieChart.legend.textColor = resources.getColor(R.color.text_primary, null)

        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    private fun setupLineChart(entries: List<MoodEntry>) {
        if (entries.isEmpty()) return

        val grouped = mutableMapOf<String, MutableList<Int>>()
        for (entry in entries) {
            if (!grouped.containsKey(entry.date)) {
                grouped[entry.date] = mutableListOf()
            }
            grouped[entry.date]?.add(entry.moodValue)
        }

        val lineEntries = mutableListOf<Entry>()
        var index = 0f

        val sortedDates = grouped.keys.sorted()

        for (date in sortedDates) {
            val values = grouped[date] ?: emptyList()
            if (values.isNotEmpty()) {
                val avg = values.average().toFloat()
                lineEntries.add(Entry(index, avg))
                index += 1f
            }
        }

        if (lineEntries.isEmpty()) return

        val dataSet = LineDataSet(lineEntries, "Настроение")
        dataSet.color = resources.getColor(R.color.primary, null)
        dataSet.valueTextColor = resources.getColor(R.color.text_primary, null)
        dataSet.setCircleColor(resources.getColor(R.color.primary, null))
        dataSet.circleRadius = 6f
        dataSet.lineWidth = 3f
        dataSet.valueTextSize = 12f
        dataSet.setDrawValues(true)

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.description.isEnabled = false
        lineChart.invalidate()
    }

    private fun shareStats() {
        val entries = if (currentMonth == -1) allEntries else {
            allEntries.filter {
                val month = it.date.split("-")[1].toInt()
                month == currentMonth + 1
            }
        }

        if (entries.isEmpty()) {
            Toast.makeText(requireContext(), "Нет данных для экспорта", Toast.LENGTH_SHORT).show()
            return
        }

        val avgMood = entries.map { it.moodValue }.average()
        val bestMood = entries.maxByOrNull { it.moodValue }
        val worstMood = entries.minByOrNull { it.moodValue }

        val text = buildString {
            appendLine("📊 Моя статистика настроения")
            appendLine("=".repeat(30))
            appendLine()
            appendLine("📝 Записей: ${entries.size}")
            appendLine("⭐ Средний балл: ${String.format("%.1f", avgMood)} / 5")
            appendLine("🏆 Лучший день: ${bestMood?.moodEmoji} ${bestMood?.moodLabel}")
            appendLine("📉 Худший день: ${worstMood?.moodEmoji} ${worstMood?.moodLabel}")
            appendLine()
            appendLine("📱 Сделано в приложении \"Дневник настроения\"")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(shareIntent, "Поделиться статистикой"))
    }

    private fun exportToText() {
        val entries = if (currentMonth == -1) allEntries else {
            allEntries.filter {
                val month = it.date.split("-")[1].toInt()
                month == currentMonth + 1
            }
        }

        if (entries.isEmpty()) {
            Toast.makeText(requireContext(), "Нет данных для экспорта", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val fileName = "stats_${dateFormat.format(Date())}.txt"
            val file = File(requireContext().getExternalFilesDir(null), fileName)

            file.printWriter().use { writer ->
                writer.println("📊 СТАТИСТИКА НАСТРОЕНИЯ")
                writer.println("=".repeat(40))
                writer.println("Дата экспорта: ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())}")
                writer.println("=".repeat(40))
                writer.println()

                val avgMood = entries.map { it.moodValue }.average()
                val bestMood = entries.maxByOrNull { it.moodValue }
                val worstMood = entries.minByOrNull { it.moodValue }
                val totalEntries = entries.size

                writer.println("📝 Всего записей: $totalEntries")
                writer.println("⭐ Средний балл: ${String.format("%.1f", avgMood)} / 5")
                writer.println("🏆 Лучший день: ${bestMood?.moodEmoji} ${bestMood?.moodLabel} (${bestMood?.date})")
                writer.println("📉 Худший день: ${worstMood?.moodEmoji} ${worstMood?.moodLabel} (${worstMood?.date})")
                writer.println()
                writer.println("-".repeat(40))
                writer.println()

                writer.println("📝 ВСЕ ЗАПИСИ:")
                writer.println()

                entries.reversed().forEach { entry ->
                    writer.println("📅 ${entry.date}")
                    writer.println("😊 ${entry.moodEmoji} ${entry.moodLabel} (${entry.moodValue}/5)")
                    if (entry.title.isNotEmpty()) {
                        writer.println("📌 ${entry.title}")
                    }
                    if (entry.note.isNotEmpty()) {
                        writer.println("📝 ${entry.note}")
                    }
                    if (entry.tags.isNotEmpty()) {
                        writer.println("🏷️ Теги: ${entry.tags.joinToString(", ")}")
                    }
                    writer.println("-".repeat(20))
                }
            }

            Toast.makeText(requireContext(), "✅ Файл сохранён: $fileName", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "❌ Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        private var items = listOf<MoodEntry>()

        fun submitList(list: List<MoodEntry>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = items[position]
            holder.bind(entry)
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val dateText: TextView = itemView.findViewById(R.id.historyDate)
            private val moodText: TextView = itemView.findViewById(R.id.historyMood)
            private val noteText: TextView = itemView.findViewById(R.id.historyNote)

            fun bind(entry: MoodEntry) {
                dateText.text = entry.date
                moodText.text = "${entry.moodEmoji} ${entry.moodLabel}"
                noteText.text = entry.note.ifEmpty { "(без заметки)" }
            }
        }
    }
}
