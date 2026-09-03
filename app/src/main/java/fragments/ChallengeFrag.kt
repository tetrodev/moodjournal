package com.example.moodjournal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moodjournal.R
import com.example.moodjournal.data.Challenge
import com.example.moodjournal.data.ChallengeRepository
import java.text.SimpleDateFormat
import java.util.*

class ChallengeFrag : Fragment() {

    private lateinit var repository: ChallengeRepository
    private lateinit var dailyChallengeText: TextView
    private lateinit var challengeCategoryText: TextView
    private lateinit var completeButton: Button
    private lateinit var newChallengeButton: Button
    private lateinit var historyRecycler: RecyclerView

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = ChallengeRepository(requireContext())

        dailyChallengeText = view.findViewById(R.id.dailyChallengeText)
        challengeCategoryText = view.findViewById(R.id.challengeCategoryText)
        completeButton = view.findViewById(R.id.completeButton)
        newChallengeButton = view.findViewById(R.id.newChallengeButton)
        historyRecycler = view.findViewById(R.id.challengeHistoryRecycler)

        loadDailyChallenge()

        completeButton.setOnClickListener {
            completeChallenge()
        }

        newChallengeButton.setOnClickListener {
            loadRandomChallenge()
        }

        loadHistory()
    }

    private fun loadDailyChallenge() {
        val challenge = repository.getDailyChallenge()
        val today = dateFormat.format(Date())
        val isCompleted = repository.isChallengeCompleted(today)

        dailyChallengeText.text = challenge.title
        challengeCategoryText.text = "🏷️ ${challenge.category}"

        if (isCompleted) {
            completeButton.text = "✅ Выполнено"
            completeButton.isEnabled = false
            completeButton.setBackgroundColor(resources.getColor(R.color.mood_excellent, null))
        } else {
            completeButton.text = "🎯 Выполнить"
            completeButton.isEnabled = true
            completeButton.setBackgroundColor(resources.getColor(R.color.primary, null))
        }
    }

    private fun loadRandomChallenge() {
        val challenge = repository.getAllChallenges().shuffled().first()
        dailyChallengeText.text = challenge.title
        challengeCategoryText.text = "🏷️ ${challenge.category}"

        completeButton.text = "🎯 Выполнить"
        completeButton.isEnabled = true
        completeButton.setBackgroundColor(resources.getColor(R.color.primary, null))
    }

    private fun completeChallenge() {
        val today = dateFormat.format(Date())
        repository.completeChallenge(today)

        Toast.makeText(requireContext(), "🎉 Челлендж выполнен! Отлично!", Toast.LENGTH_SHORT).show()

        completeButton.text = "✅ Выполнено"
        completeButton.isEnabled = false
        completeButton.setBackgroundColor(resources.getColor(R.color.mood_excellent, null))

        loadHistory()
    }

    private fun loadHistory() {
        val completedIds = repository.getCompletedIds()
        val history = completedIds.sorted().reversed().toList()

        historyRecycler.layoutManager = LinearLayoutManager(requireContext())
        historyRecycler.adapter = ChallengeHistoryAdapter(history)
    }

    class ChallengeHistoryAdapter(private val dates: List<String>) :
        RecyclerView.Adapter<ChallengeHistoryAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chall_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val date = dates[position]
            holder.dateText.text = date
            holder.statusText.text = "✅ Выполнено"
        }

        override fun getItemCount(): Int = dates.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val dateText: TextView = itemView.findViewById(R.id.challengeDate)
            val statusText: TextView = itemView.findViewById(R.id.challengeStatus)
        }
    }
}