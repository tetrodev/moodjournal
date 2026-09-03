package com.example.moodjournal.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moodjournal.R
import com.example.moodjournal.data.MoodEntry

class TrendAdapter(
    private var items: List<MoodEntry> = emptyList()
) : RecyclerView.Adapter<TrendAdapter.TrendViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trend, parent, false)
        return TrendViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrendViewHolder, position: Int) {
        val entry = items[position]
        holder.bind(entry)
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<MoodEntry>) {
        items = newItems
        notifyDataSetChanged()
    }

    class TrendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val dateText: TextView = itemView.findViewById(R.id.trendDate)
        private val moodText: TextView = itemView.findViewById(R.id.trendMood)
        private val noteText: TextView = itemView.findViewById(R.id.trendNote)

        fun bind(entry: MoodEntry) {
            dateText.text = entry.date
            val moodDisplay = "${entry.moodEmoji} ${entry.moodLabel}"
            moodText.text = moodDisplay
            noteText.text = if (entry.note.isNotEmpty()) {
                entry.note
            } else {
                "(без заметки)"
            }
        }
    }
}


