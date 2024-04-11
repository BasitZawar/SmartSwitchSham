package com.example.ss_new.fragments.files_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ss_new.activites.FileToSelectActivity
import com.example.ss_new.adapters.recycler_adapter.AudioRecyclerAdapter
import com.example.ss_new.databinding.FragmentMusicBinding
import com.example.ss_new.app_utils.AllFilesUtils
import com.example.ss_new.app_utils.FileSelectionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MusicFragment : Fragment() {

    lateinit var binding : FragmentMusicBinding
    lateinit var audioAdapter : AudioRecyclerAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMusicBinding.inflate(layoutInflater)


        audioAdapter = AudioRecyclerAdapter(listOf(),requireActivity(),object  : FileSelectionListener {
            override fun clicked(path: String) {
            }
            override fun selected(b: Boolean, path: String) {
                if(FileToSelectActivity.selectedFileList.any { it.path == path }){
                    FileToSelectActivity.selectedFileList.removeAll { it.path == path }
                }else{
                    val index = AllFilesUtils.allAudioFromDevice.indexOfFirst { it.path== path }
                    FileToSelectActivity.selectedFileList.add(AllFilesUtils.allAudioFromDevice[index])
                }
                if(activity is FileToSelectActivity){
                    (activity as FileToSelectActivity).updateSelected()
                }
            }
        })
        binding.fileExploreRec.layoutManager = LinearLayoutManager(requireActivity() )
        binding.fileExploreRec.adapter = audioAdapter
        if(AllFilesUtils.allAudioFromDevice.isEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                AllFilesUtils.retrieveAllFiles(requireActivity(), AllFilesUtils.audio)
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
        AllFilesUtils.allAudioFromDevice.apply {
            if(this.isEmpty()){
                binding.fileExploreRec.visibility= View.GONE
                binding.layNothing.visibility= View.VISIBLE
            }else{
                binding.fileExploreRec.visibility= View.VISIBLE
                binding.layNothing.visibility= View.GONE
            }
            audioAdapter.updateItemList(this)
        }
    }

}