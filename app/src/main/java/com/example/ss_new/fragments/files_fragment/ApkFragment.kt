package com.example.ss_new.fragments.files_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ss_new.activites.FileToSelectActivity
import com.example.ss_new.activites.FileToSelectActivity.Companion.selectedFileList
import com.example.ss_new.adapters.recycler_adapter.ApkRecyclerAdapter
import com.example.ss_new.databinding.FragmentApkBinding
import com.example.ss_new.app_utils.AllFilesUtils
import com.example.ss_new.app_utils.FileSelectionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ApkFragment : Fragment() {

    val binding by lazy {
        FragmentApkBinding.inflate(layoutInflater)
    }
    lateinit var adapter : ApkRecyclerAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        adapter= ApkRecyclerAdapter(listOf(), requireActivity(),object  : FileSelectionListener {
            override fun selected(b: Boolean, path: String) {
                if(selectedFileList.any { it.path == path }){
                    selectedFileList.removeAll { it.path == path }
                }else{
                    val index = AllFilesUtils.allApkFromDevice.indexOfFirst { it.path== path }
                    selectedFileList.add(AllFilesUtils.allApkFromDevice[index])
                }
                if(activity is FileToSelectActivity){
                    (activity as FileToSelectActivity).updateSelected()
                }
            }
        })

        binding.fileExploreRec.layoutManager = LinearLayoutManager(requireActivity())
        binding.fileExploreRec.adapter = adapter

        if(AllFilesUtils.allApkFromDevice.isEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                AllFilesUtils.retrieveAllFiles(requireActivity(), AllFilesUtils.apk)
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
        AllFilesUtils.allApkFromDevice.apply {
            if(this.isEmpty()){
                binding.fileExploreRec.visibility= View.GONE
                binding.layNothing.visibility= View.VISIBLE
            }else{
                binding.fileExploreRec.visibility= View.VISIBLE
                binding.layNothing.visibility= View.GONE
            }
            adapter.updateItemList(this)
        }
    }

}