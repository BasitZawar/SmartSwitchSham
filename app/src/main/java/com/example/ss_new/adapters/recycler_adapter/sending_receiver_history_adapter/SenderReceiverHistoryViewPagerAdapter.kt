package com.example.ss_new.adapters.recycler_adapter.sending_receiver_history_adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.ss_new.fragments.history_frag.HistoryReceiveFragment
import com.example.ss_new.fragments.history_frag.HistorySendFragment

class SenderReceiverHistoryViewPagerAdapter(fragmentManager: FragmentManager, var behavior:Int, var sendFrag: HistorySendFragment, var receiveFrag: HistoryReceiveFragment) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> sendFrag
            1 -> receiveFrag
            else -> sendFrag
        }
    }

    override fun getCount(): Int {
        return behavior
    }

}