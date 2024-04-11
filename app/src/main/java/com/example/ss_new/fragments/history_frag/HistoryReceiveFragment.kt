package com.example.ss_new.fragments.history_frag

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ss_new.database.DBHelper
import com.example.ss_new.database.FilesEntity
import com.example.ss_new.databinding.FragmentHistoryReceiveBinding
import com.example.ss_new.adapters.recycler_adapter.sending_receiver_history_adapter.RecAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class HistoryReceiveFragment : Fragment() {

    val binding by lazy {
        FragmentHistoryReceiveBinding.inflate(layoutInflater)
    }
    private var receiveList = ArrayList<FilesEntity>()
    lateinit var adapter: RecAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        adapter = RecAdapter(arrayListOf(),requireActivity())
        binding.receiveRec.layoutManager = LinearLayoutManager(requireActivity())
        binding.receiveRec.adapter = adapter

        CoroutineScope(Dispatchers.IO).launch {
            val l = DBHelper.getDB(requireActivity()).sSwitchDao()?.getAllReceivedFile() as ArrayList<FilesEntity>
            for(i in l){
                if(File(i.path).exists()){
                    receiveList.add(i)
                }else{
                    DBHelper.getDB(requireActivity()).sSwitchDao()?.deleteByPath(i.path)
                }
            }
            withContext(Dispatchers.Main){
                Timber.e("received files : ${l.size}")
                receiveList.sortBy { parseDateTime(it.date) }
                binding.progress.visibility = View.GONE
                if(receiveList.size == 0){
                    binding.layNoFiles.visibility = View.VISIBLE
                    binding.receiveRec.visibility = View.GONE
                }else{
                    binding.layNoFiles.visibility = View.GONE
                    binding.receiveRec.visibility = View.VISIBLE
                }
                adapter.setData(receiveList)
            }
        }

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