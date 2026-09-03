package com.example.moodjournal.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.moodjournal.ui.fragments.*

class MainPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TodayFragment()
            1 -> CalendarFragment()
            2 -> ProfileFragment()
            3 -> ArticlesFragment()
            else -> SettingsFragment()
        }
    }
}