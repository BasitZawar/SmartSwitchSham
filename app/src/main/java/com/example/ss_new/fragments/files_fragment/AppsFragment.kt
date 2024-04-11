package com.example.ss_new.fragments.files_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ss_new.activites.FileToSelectActivity
import com.example.ss_new.adapters.recycler_adapter.AppsRecyclerAdapter
import com.example.ss_new.databinding.FragmentAppsBinding
import com.example.ss_new.app_utils.AllFilesUtils
import com.example.ss_new.app_utils.FileSelectionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AppsFragment : Fragment() {


    val binding by lazy{
        FragmentAppsBinding.inflate(layoutInflater)
    }

    lateinit var adapter :AppsRecyclerAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        adapter = AppsRecyclerAdapter(listOf(), requireActivity(),object  : FileSelectionListener {
            override fun selected(b: Boolean, path: String) {
                if(FileToSelectActivity.selectedFileList.any { it.path == path }){
                    FileToSelectActivity.selectedFileList.removeAll { it.path == path }
                }else{
                    val index = AllFilesUtils.allAppsFromDevice.indexOfFirst { it.path== path }
                    FileToSelectActivity.selectedFileList.add(AllFilesUtils.allAppsFromDevice[index])
                }
                if(activity is FileToSelectActivity){
                    (activity as FileToSelectActivity).updateSelected()
                }
            }
        })
        binding.fileExploreRec.layoutManager = LinearLayoutManager(requireActivity())
        binding.fileExploreRec.adapter = adapter

        if(AllFilesUtils.allAppsFromDevice.isEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                AllFilesUtils.getInstalledApps(requireActivity())
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
        AllFilesUtils.allAppsFromDevice.apply {
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