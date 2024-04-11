package com.example.ss_new.fragments.files_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ss_new.activites.FileToSelectActivity
import com.example.ss_new.adapters.recycler_adapter.ImageRecyclerAdapter
import com.example.ss_new.databinding.FragmentImageBinding
import com.example.ss_new.app_utils.AllFilesUtils
import com.example.ss_new.app_utils.FileSelectionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageFragment : Fragment() {

    lateinit var binding : FragmentImageBinding
    lateinit var imageAdapter : ImageRecyclerAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageBinding.inflate(layoutInflater)

        imageAdapter = ImageRecyclerAdapter(listOf(),requireActivity(),object  : FileSelectionListener {
            override fun clicked(path: String) {
            }
            override fun selected(b: Boolean, path: String) {
                if(FileToSelectActivity.selectedFileList.any { it.path == path }){
                    FileToSelectActivity.selectedFileList.removeAll { it.path == path }
                }else{
                    val index = AllFilesUtils.allImagesFromDevice.indexOfFirst { it.path== path }
                    FileToSelectActivity.selectedFileList.add(AllFilesUtils.allImagesFromDevice[index])
                }
                if(activity is FileToSelectActivity){
                    (activity as FileToSelectActivity).updateSelected()
                }
            }
        })

        binding.fileExploreRec.layoutManager = GridLayoutManager(requireActivity(),4)
        binding.fileExploreRec.adapter = imageAdapter
        if(AllFilesUtils.allImagesFromDevice.isEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                AllFilesUtils.retrieveAllFiles(requireActivity(), AllFilesUtils.image)
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
        AllFilesUtils.allImagesFromDevice.apply {
            if(this.isEmpty()){
                binding.fileExploreRec.visibility= View.GONE
                binding.layNothing.visibility= View.VISIBLE
            }else{
                binding.fileExploreRec.visibility= View.VISIBLE
                binding.layNothing.visibility= View.GONE
            }
            imageAdapter.updateItemList(this)
        }
    }

}