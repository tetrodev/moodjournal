package com.example.moodjournal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.moodjournal.R
import com.example.moodjournal.data.QuoteRepository
import java.text.SimpleDateFormat
import java.util.*

class PredictionFrag : Fragment() {

    private lateinit var quoteRepository: QuoteRepository
    private lateinit var predictionText: TextView
    private lateinit var dateText: TextView
    private lateinit var newPredictionButton: Button
    private lateinit var historyText: TextView

    private val history = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pred, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        quoteRepository = QuoteRepository()

        predictionText = view.findViewById(R.id.predictionText)
        dateText = view.findViewById(R.id.dateText)
        newPredictionButton = view.findViewById(R.id.newPredictionButton)
        historyText = view.findViewById(R.id.historyText)

        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))
        dateText.text = dateFormat.format(Date())

        showDailyPrediction()

        newPredictionButton.setOnClickListener {
            showRandomPrediction()
        }
    }

    private fun showDailyPrediction() {
        val prediction = quoteRepository.getDailyPrediction()
        predictionText.text = prediction
        addToHistory(prediction)
    }

    private fun showRandomPrediction() {
        val prediction = quoteRepository.getRandomPrediction()
        predictionText.text = prediction
        addToHistory(prediction)
    }

    private fun addToHistory(prediction: String) {
        if (!history.contains(prediction)) {
            history.add(0, prediction)
            if (history.size > 10) history.removeAt(history.size - 1)
            updateHistory()
        }
    }

    private fun updateHistory() {
        historyText.text = if (history.isNotEmpty()) {
            "📜 История предсказаний:\n\n" + history.joinToString("\n\n") { "• $it" }
        } else {
            "📜 История предсказаний\n\nПока нет предсказаний"
        }
    }
}