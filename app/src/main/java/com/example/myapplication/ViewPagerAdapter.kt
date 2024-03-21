package com.example.myapplication

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.fragment.app.FragmentActivity
import com.example.myapplication.Fragements.ForYouFrag
import com.example.myapplication.Fragements.ToptrackFrag

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2 // The total number of tabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ForYouFrag() // Assuming ForYouFrag is a valid Fragment
            1 -> ToptrackFrag() // Assuming ToptrackFrag is a valid Fragment
            else -> Fragment() // Fallback
        }
    }
}
