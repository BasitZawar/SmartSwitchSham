package com.example.ss_new.fragments.files_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ss_new.activites.FileToSelectActivity
import com.example.ss_new.adapters.recycler_adapter.DocsRecyclerAdapter
import com.example.ss_new.databinding.FragmentDocsBinding
import com.example.ss_new.app_utils.AllFilesUtils
import com.example.ss_new.app_utils.FileSelectionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DocsFragment : Fragment() {

    lateinit var binding : FragmentDocsBinding
    lateinit var docsAdapter :DocsRecyclerAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDocsBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment

        docsAdapter = DocsRecyclerAdapter(listOf(), requireActivity(),object  : FileSelectionListener {
            override fun selected(b: Boolean, path: String) {
                if(FileToSelectActivity.selectedFileList.any { it.path == path }){
                    FileToSelectActivity.selectedFileList.removeAll { it.path == path }
                }else{
                    val index = AllFilesUtils.allDocFromDevice.indexOfFirst { it.path== path }
                    FileToSelectActivity.selectedFileList.add(AllFilesUtils.allDocFromDevice[index])
                }
                if(activity is FileToSelectActivity){
                    (activity as FileToSelectActivity).updateSelected()
                }
            }
        })

        binding.fileExploreRec.layoutManager = LinearLayoutManager(requireActivity())
        binding.fileExploreRec.adapter = docsAdapter

        if(AllFilesUtils.allDocFromDevice.isEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                AllFilesUtils.retrieveAllFiles(requireActivity(), AllFilesUtils.docs)
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
        AllFilesUtils.allDocFromDevice.apply {
            if(this.isEmpty()){
                binding.fileExploreRec.visibility= View.GONE
                binding.layNothing.visibility= View.VISIBLE
            }else{
                binding.fileExploreRec.visibility= View.VISIBLE
                binding.layNothing.visibility= View.GONE
            }
            docsAdapter.updateItemList(this)
        }
    }
}