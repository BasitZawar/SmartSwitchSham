package com.example.ss_new.fragments.files_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ss_new.activites.FileToSelectActivity
import com.example.ss_new.adapters.recycler_adapter.DownRecyclerAdapter
import com.example.ss_new.database.FilesEntity
import com.example.ss_new.databinding.FragmentDownloadsBinding
import com.example.ss_new.app_utils.AllFilesUtils
import com.example.ss_new.app_utils.FileSelectionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DownloadsFragment : Fragment() {

    lateinit var binding : FragmentDownloadsBinding
    private var list = arrayListOf<FilesEntity>()
    lateinit var adapter : DownRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDownloadsBinding.inflate(layoutInflater)

        adapter = DownRecyclerAdapter(listOf(), requireActivity(),object  : FileSelectionListener {
            override fun selected(b: Boolean, path: String) {
                if(FileToSelectActivity.selectedFileList.any { it.path == path }){
                    FileToSelectActivity.selectedFileList.removeAll { it.path == path }
                }else{
                    val index = AllFilesUtils.allDownloadsFromDevice.indexOfFirst { it.path== path }
                    FileToSelectActivity.selectedFileList.add(AllFilesUtils.allDownloadsFromDevice[index])
                }
                if(activity is FileToSelectActivity){
                    (activity as FileToSelectActivity).updateSelected()
                }
            }
        })

        binding.fileExploreRec.layoutManager = LinearLayoutManager(requireActivity())
        binding.fileExploreRec.adapter = adapter

        if(AllFilesUtils.allDownloadsFromDevice.isEmpty()){
            CoroutineScope(Dispatchers.IO).launch {
                AllFilesUtils.getAllFilesInDownloads()
                withContext(Dispatchers.Main) {
                    setListToAdapter()
                }
            }
        }else{
          setListToAdapter()
        }

        return binding.root
    }
    private fun setListToAdapter(){
        binding.progress.visibility = View.GONE
        AllFilesUtils.allDownloadsFromDevice.apply {
            if (this.isEmpty()) {
                binding.fileExploreRec.visibility = View.GONE
                binding.layNothing.visibility = View.VISIBLE
            } else {
                binding.fileExploreRec.visibility = View.VISIBLE
                binding.layNothing.visibility = View.GONE
            }
            adapter.updateItemList(this)
        }
    }

}