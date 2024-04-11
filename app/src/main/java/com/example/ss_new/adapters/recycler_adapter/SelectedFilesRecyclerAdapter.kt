package com.example.ss_new.adapters.recycler_adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ss_new.R
import com.example.ss_new.app_utils.AllFilesUtils
import com.example.ss_new.database.FilesEntity
import com.example.ss_new.app_utils.FileSelectionListener
import java.io.File

class SelectedFilesRecyclerAdapter(
    var docsList: List<FilesEntity>,
    var context: Context, private var listener: FileSelectionListener
)  : RecyclerView.Adapter<SelectedFilesRecyclerAdapter.DocsViewHolder>() {

    inner class DocsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val checkBox: ImageView = itemView.findViewById(R.id.removeSelection)
        fun bind(position: Int) {
            itemView.findViewById<TextView>(R.id.itemAudioName).text =
                File(docsList[position].path).name
            itemView.findViewById<TextView>(R.id.itemAudioSize).text =
                AllFilesUtils.changeSizeToFormat(File(docsList[position].path).length())

            when (docsList[position].fileType) {
                AllFilesUtils.audio -> {
                    itemView.findViewById<ImageView>(R.id.itemImgAudio).setImageResource(R.drawable.icon_music_list)
                }
                AllFilesUtils.video -> {
                    itemView.findViewById<ImageView>(R.id.itemImgAudio).setImageResource(R.drawable.icon_video_list)
                }
                AllFilesUtils.docs -> {
                    itemView.findViewById<ImageView>(R.id.itemImgAudio).setImageResource(R.drawable.icon_docs_list)
                }
                AllFilesUtils.image -> {
                    itemView.findViewById<ImageView>(R.id.itemImgAudio).setImageResource(R.drawable.icon_img_list)
                }
                AllFilesUtils.apk -> {
                    itemView.findViewById<ImageView>(R.id.itemImgAudio).setImageResource(R.drawable.icon_apk_list)
                }
                AllFilesUtils.app -> {
                    itemView.findViewById<ImageView>(R.id.itemImgAudio).setImageResource(R.drawable.icon_apps_list)
                }
                AllFilesUtils.download -> {
                    itemView.findViewById<ImageView>(R.id.itemImgAudio).setImageResource(R.drawable.icon_download_list)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocsViewHolder {
        return DocsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_selected, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return docsList.size
    }


    override fun onBindViewHolder(holder: DocsViewHolder, position: Int) {
        holder.checkBox.setOnClickListener {
            listener.clicked(docsList[position].path)
        }
        holder.bind(position)
    }

    fun updateItemList(audioList: List<FilesEntity>) {
        this.docsList = audioList
        notifyDataSetChanged()
    }
}