package com.example.moodjournal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.moodjournal.R
import com.example.moodjournal.data.MoodEntry
import com.example.moodjournal.data.MoodRepository
import java.text.SimpleDateFormat
import java.util.*

class TodayFragment : Fragment() {


    private lateinit var repository: MoodRepository
    private lateinit var moodContainer: LinearLayout
    private lateinit var titleInput: EditText
    private lateinit var noteInput: EditText
    private lateinit var saveButton: Button
    private lateinit var selectedMoodView: TextView


    private lateinit var tagInput: EditText
    private lateinit var addTagButton: Button
    private lateinit var tagContainer: LinearLayout

    private var selectedMood: MoodEntry.Mood? = null
    private val tags = mutableListOf<String>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_today, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = MoodRepository(requireContext())

        moodContainer = view.findViewById(R.id.moodContainer)
        titleInput = view.findViewById(R.id.titleInput)
        noteInput = view.findViewById(R.id.noteInput)
        saveButton = view.findViewById(R.id.saveButton)
        selectedMoodView = view.findViewById(R.id.selectedMood)


        tagInput = view.findViewById(R.id.tagInput)
        addTagButton = view.findViewById(R.id.addTagButton)
        tagContainer = view.findViewById(R.id.tagContainer)

        setupMoodSelection()
        setupTags()
        loadTodayEntry()

        saveButton.setOnClickListener {
            saveTodayEntry()
        }
    }

    private fun setupMoodSelection() {
        val moods = MoodEntry.Mood.values()

        for (mood in moods) {
            val moodView = layoutInflater.inflate(R.layout.item_mood, moodContainer, false)
            val emojiView = moodView.findViewById<TextView>(R.id.moodEmoji)
            val labelView = moodView.findViewById<TextView>(R.id.moodLabel)

            emojiView.text = mood.emoji
            labelView.text = mood.label

            moodView.setOnClickListener {
                selectedMood = mood
                selectedMoodView.text = "${mood.emoji} ${mood.label}"
                selectedMoodView.visibility = View.VISIBLE

                for (i in 0 until moodContainer.childCount) {
                    val child = moodContainer.getChildAt(i)
                    child.setBackgroundResource(R.drawable.mood_item_border)
                }
                moodView.setBackgroundResource(R.drawable.mood_item_selected)
            }

            moodContainer.addView(moodView)
        }
    }

    private fun setupTags() {
        addTagButton.setOnClickListener {
            val tag = tagInput.text.toString().trim()
            if (tag.isNotEmpty() && !tags.contains(tag)) {
                tags.add(tag)
                updateTagContainer()
                tagInput.text.clear()
            }
        }
    }

    private fun updateTagContainer() {
        tagContainer.removeAllViews()
        for (tag in tags) {
            val tagView = layoutInflater.inflate(R.layout.item_tag, tagContainer, false)
            val tagText = tagView.findViewById<TextView>(R.id.tagText)
            val removeButton = tagView.findViewById<ImageButton>(R.id.removeTag)

            tagText.text = "#$tag"
            removeButton.setOnClickListener {
                tags.remove(tag)
                updateTagContainer()
            }

            tagContainer.addView(tagView)
        }
    }

    private fun loadTodayEntry() {
        val today = dateFormat.format(Date())
        val entry = repository.getEntry(today)

        entry?.let {
            selectedMood = MoodEntry.Mood.fromValue(it.moodValue)
            selectedMood?.let { mood ->
                selectedMoodView.text = "${mood.emoji} ${mood.label}"
                selectedMoodView.visibility = View.VISIBLE
            }
            titleInput.setText(it.title)
            noteInput.setText(it.note)
            tags.clear()
            tags.addAll(it.tags)
            updateTagContainer()
        }
    }

    private fun saveTodayEntry() {
        if (selectedMood == null) {
            Toast.makeText(requireContext(), "Выберите настроение", Toast.LENGTH_SHORT).show()
            return
        }

        val today = dateFormat.format(Date())
        val entry = MoodEntry(
            date = today,
            moodValue = selectedMood!!.value,
            moodEmoji = selectedMood!!.emoji,
            moodLabel = selectedMood!!.label,
            title = titleInput.text.toString().trim(),
            note = noteInput.text.toString().trim(),
            tags = tags.toList()
        )

        repository.saveEntry(entry)
        Toast.makeText(requireContext(), "✅ Сохранено!", Toast.LENGTH_SHORT).show()


        titleInput.text.clear()
        noteInput.text.clear()
        tags.clear()
        updateTagContainer()
        selectedMood = null
        selectedMoodView.visibility = View.GONE
        for (i in 0 until moodContainer.childCount) {
            val child = moodContainer.getChildAt(i)
            child.setBackgroundResource(R.drawable.mood_item_border)
        }
    }
}





