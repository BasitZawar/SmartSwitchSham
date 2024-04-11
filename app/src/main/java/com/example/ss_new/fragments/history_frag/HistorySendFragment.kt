package com.example.ss_new.fragments.history_frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ss_new.database.DBHelper
import com.example.ss_new.database.FilesEntity
import com.example.ss_new.databinding.FragmentHistorySendBinding
import com.example.ss_new.adapters.recycler_adapter.sending_receiver_history_adapter.RecAdapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class HistorySendFragment : Fragment() {

    val binding by lazy {
        FragmentHistorySendBinding.inflate(layoutInflater)
    }
    lateinit var adapter  : RecAdapter
    private var sentList = ArrayList<FilesEntity>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        adapter = RecAdapter(arrayListOf(),requireActivity())
        binding.sendRec.layoutManager = LinearLayoutManager(requireActivity())
        binding.sendRec.adapter = adapter


        DBHelper.getDB(requireActivity()).sSwitchDao()?.getAllSentFile()?.observe(requireActivity(),
            androidx.lifecycle.Observer {
                sentList.addAll(it)
                sentList.sortBy { parseDateTime(it.date) }
                binding.progress.visibility = View.GONE
                if(sentList.size == 0){
                    binding.layNoFiles.visibility = View.VISIBLE
                    binding.sendRec.visibility = View.GONE
                }else{
                    binding.layNoFiles.visibility = View.GONE
                    binding.sendRec.visibility = View.VISIBLE
                }
                adapter.setData(sentList)
            })


        return binding.root
    }

    private fun parseDateTime(dateTimeString: String): Long {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault())
        try {
            val date = dateFormat.parse(dateTimeString)
            return date?.time ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }
}