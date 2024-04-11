package com.example.ss_new.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.ss_new.fragments.dashboard_frags.FilesFragment
import com.example.ss_new.fragments.dashboard_frags.HistoryFragment
import com.example.ss_new.fragments.dashboard_frags.HomeFragment

class MainPagerAdapter(fragmentManager: FragmentManager, var behavior:Int, var home: HomeFragment, var files: FilesFragment, var history: HistoryFragment) : FragmentPagerAdapter(fragmentManager) {


    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> files
            1 -> home
            2 -> history
            else -> home
        }
    }

    override fun getCount(): Int {
        return behavior
    }

}