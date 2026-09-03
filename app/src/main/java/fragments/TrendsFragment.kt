package com.example.moodjournal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moodjournal.R
import com.example.moodjournal.data.MoodRepository
import com.google.android.material.button.MaterialButton

class TrendsFragment : Fragment() {


    private lateinit var repository: MoodRepository
    private lateinit var statsTextView: TextView
    private lateinit var exportButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = MoodRepository(requireContext())


        statsTextView = view.findViewById(R.id.statsTextView)
        exportButton = view.findViewById(R.id.exportButton)

        loadData()

        exportButton.setOnClickListener {
            exportData()
        }
    }

    private fun loadData() {
        val entries = repository.getAllEntries()

        if (entries.isEmpty()) {
            statsTextView.text = "📭 Нет данных для отображения\nДобавьте первую запись на вкладке 'Сегодня'!"
            return
        }

        val stats = repository.getStatistics()
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

