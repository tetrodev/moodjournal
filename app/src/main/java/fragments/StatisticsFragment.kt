package com.example.moodjournal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moodjournal.R
import com.example.moodjournal.data.MoodEntry
import com.example.moodjournal.data.MoodRepository

class StatisticsFragment : Fragment() {

    private lateinit var repository: MoodRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = MoodRepository(requireContext())

        val totalCount = view.findViewById<TextView>(R.id.totalCount)
        val weekCount = view.findViewById<TextView>(R.id.weekCount)
        val avgWeek = view.findViewById<TextView>(R.id.avgWeek)
        val bestMoodText = view.findViewById<TextView>(R.id.bestMood)
        val historyRecycler = view.findViewById<RecyclerView>(R.id.historyRecycler)

        val adapter = HistoryAdapter()
        historyRecycler.layoutManager = LinearLayoutManager(requireContext())
        historyRecycler.adapter = adapter

        val allEntries = repository.getAllEntries()
        val stats = repository.getStatistics()

        totalCount.text = stats.totalEntries.toString()
        weekCount.text = stats.weekCount.toString()
        avgWeek.text = String.format("%.1f / 5", stats.avgWeek)
        bestMoodText.text = stats.bestMood?.let {
            "${it.moodEmoji} ${it.moodLabel} (${it.date})"
        } ?: "Нет данных"

        val history = allEntries.reversed()
        adapter.submitList(history)
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
                val displayText = if (entry.title.isNotEmpty()) {
                    "${entry.title} - ${entry.note}"
                } else {
                    entry.note
                }
                noteText.text = displayText.ifEmpty { "(без заметки)" }
            }
        }
    }
}