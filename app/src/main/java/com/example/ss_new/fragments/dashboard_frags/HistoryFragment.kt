package com.example.ss_new.fragments.dashboard_frags

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.viewpager.widget.ViewPager
import com.example.ss_new.R
import com.example.ss_new.databinding.FragmentHistoryBinding
import com.example.ss_new.adapters.recycler_adapter.sending_receiver_history_adapter.SenderReceiverHistoryViewPagerAdapter
import com.example.ss_new.fragments.history_frag.HistoryReceiveFragment
import com.example.ss_new.fragments.history_frag.HistorySendFragment

class HistoryFragment : Fragment() {

    private val sendFrag = HistorySendFragment()
    private val recFrag = HistoryReceiveFragment()
    lateinit var binding: FragmentHistoryBinding
    lateinit var pagerAdapter : SenderReceiverHistoryViewPagerAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(layoutInflater)

        pagerAdapter = SenderReceiverHistoryViewPagerAdapter(
            requireActivity().supportFragmentManager,
            2,
            sendFrag,
           recFrag
        )
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.currentItem = 0
        onSendClick()

        binding.laySend.setOnClickListener {
            binding.viewPager.currentItem = 0
            onSendClick()
        }
        binding.layReceive.setOnClickListener {
            binding.viewPager.currentItem = 1
            onReceiveClick()
        }
        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                if(position ==0){
                    onSendClick()
                }else if(position ==1) {
                    onReceiveClick()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        return binding.root
    }

    private fun onSendClick(){
        ImageViewCompat.setImageTintList(binding.imgSend, ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.white)))
        binding.tvSend.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
        binding.laySend.background = ContextCompat.getDrawable(requireActivity(), R.drawable.shape_send_tab_select);


        binding.layReceive.background = ContextCompat.getDrawable(requireActivity(), R.drawable.shape_receive_tab_unselect);
        ImageViewCompat.setImageTintList(binding.imgReceive, ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.receiveUnSelectBg)))
        binding.tvReceive.setTextColor(ContextCompat.getColor(requireActivity(), R.color.receiveUnSelectBg))

    }

    private fun onReceiveClick(){
        binding.layReceive.background = ContextCompat.getDrawable(requireActivity(), R.drawable.shape_receive_tab_select);
        ImageViewCompat.setImageTintList(binding.imgReceive, ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.white)))
        binding.tvReceive.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))


        binding.laySend.background = ContextCompat.getDrawable(requireActivity(), R.drawable.shape_send_tab_unselect);
        ImageViewCompat.setImageTintList(binding.imgSend, ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.sendUnSelectBg)))
        binding.tvSend.setTextColor(ContextCompat.getColor(requireActivity(), R.color.sendUnSelectBg))

    }
}