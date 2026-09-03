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
import com.example.moodjournal.data.QuoteRepository
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class QuoteFragment : Fragment() {


    private lateinit var quoteRepository: QuoteRepository
    private lateinit var dailyQuoteText: TextView
    private lateinit var dateText: TextView
    private lateinit var newQuoteButton: MaterialButton
    private lateinit var quoteHistoryRecycler: RecyclerView

    private val quoteHistory = mutableListOf<String>()
    private val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frament_quote, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        quoteRepository = QuoteRepository()


        dailyQuoteText = view.findViewById(R.id.dailyQuoteText)
        dateText = view.findViewById(R.id.dateText)
        newQuoteButton = view.findViewById(R.id.newQuoteButton)
        quoteHistoryRecycler = view.findViewById(R.id.quoteHistoryRecycler)

        dateText.text = dateFormat.format(Date())
        showDailyQuote()

        newQuoteButton.setOnClickListener {
            showRandomQuote()
        }

        quoteHistoryRecycler.layoutManager = LinearLayoutManager(requireContext())
        quoteHistoryRecycler.adapter = QuoteHistoryAdapter(quoteHistory)
    }

    private fun showDailyQuote() {
        val quote = quoteRepository.getDailyQuote()
        dailyQuoteText.text = quote
        if (!quoteHistory.contains(quote)) {
            quoteHistory.add(0, quote)
            quoteHistoryRecycler.adapter?.notifyDataSetChanged()
        }
    }

    private fun showRandomQuote() {
        val quote = quoteRepository.getRandomQuote()
        dailyQuoteText.text = quote
        if (!quoteHistory.contains(quote)) {
            quoteHistory.add(0, quote)
            quoteHistoryRecycler.adapter?.notifyDataSetChanged()
        }
    }

    class QuoteHistoryAdapter(private val quotes: List<String>) :
        RecyclerView.Adapter<QuoteHistoryAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_quote_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.quoteText.text = quotes[position]
        }

        override fun getItemCount(): Int = quotes.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val quoteText: TextView = itemView.findViewById(R.id.quoteHistoryText)
        }
    }
}
