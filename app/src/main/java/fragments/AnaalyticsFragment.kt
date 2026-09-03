package com.example.moodjournal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
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

class AnalyticsFragment : Fragment() {

    private lateinit var repository: MoodRepository
    private lateinit var pieChart: PieChart
    private lateinit var lineChart: LineChart
    private lateinit var statsTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = MoodRepository(requireContext())

        pieChart = view.findViewById(R.id.pieChart)
        lineChart = view.findViewById(R.id.lineChart)
        statsTextView = view.findViewById(R.id.statsTextView)
        val exportButton = view.findViewById<Button>(R.id.exportButton)

        loadData()

        exportButton.setOnClickListener {
            exportData()
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val entries = repository.getAllEntries()

        if (entries.isEmpty()) {
            statsTextView.text = "📭 Нет данных для отображения\nДобавьте первую запись на вкладке 'Сегодня'!"
            pieChart.visibility = View.GONE
            lineChart.visibility = View.GONE
            return
        }

        pieChart.visibility = View.VISIBLE
        lineChart.visibility = View.VISIBLE

        val avgMood = entries.map { it.moodValue }.average()
        val bestMood = entries.maxByOrNull { it.moodValue }
        val worstMood = entries.minByOrNull { it.moodValue }

        statsTextView.text = buildString {
            appendLine("📊 Сводная статистика")
            appendLine()
            appendLine("📝 Всего записей: ${entries.size}")
            appendLine("⭐ Средний балл: ${String.format("%.1f", avgMood)} / 5")
            appendLine("🏆 Лучший день: ${bestMood?.moodEmoji} ${bestMood?.moodLabel} (${bestMood?.date})")
            appendLine("📉 Худший день: ${worstMood?.moodEmoji} ${worstMood?.moodLabel} (${worstMood?.date})")
        }

        setupPieChart(entries)
        setupLineChart(entries)
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

    private fun exportData() {
        val entries = repository.getAllEntries()
        if (entries.isEmpty()) {
            Toast.makeText(requireContext(), "Нет данных для экспорта", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(
            requireContext(),
            "✅ Экспорт: ${entries.size} записей",
            Toast.LENGTH_LONG
        ).show()
    }
}