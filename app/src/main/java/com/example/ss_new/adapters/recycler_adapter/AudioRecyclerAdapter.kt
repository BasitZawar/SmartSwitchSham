package com.example.ss_new.adapters.recycler_adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ss_new.R
import com.example.ss_new.database.FilesEntity
import com.example.ss_new.app_utils.FileSelectionListener
import java.io.File

class AudioRecyclerAdapter(
    var audioList: List<FilesEntity>,
    var context: Context, private var listener: FileSelectionListener
)  : RecyclerView.Adapter<AudioRecyclerAdapter.AudioViewHolder>() {

    inner class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val checkBox: CheckBox = itemView.findViewById(R.id.chkSelected)
        fun bind(position: Int){
            itemView.findViewById<TextView>(R.id.itemAudioName).text = File(audioList[position].path).name
            itemView.findViewById<ImageView>(R.id.itemImgAudio).setImageResource(R.drawable.icon_music_list)

            itemView.setOnClickListener {
                if(!checkBox.isChecked){
                    checkBox.isChecked = true
                    listener.selected(true,audioList[position].path)
                }else{
                    checkBox.isChecked = false
                    listener.selected(false,audioList[position].path)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        return AudioViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_audios, parent, false)
            )
    }

    override fun getItemCount(): Int {
        return audioList.size
        }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {



        holder.checkBox.setOnClickListener {
            if(holder.checkBox.isChecked){
                listener.selected(true,audioList[position].path)
            }else{
                listener.selected(false,audioList[position].path)
            }
        }
        holder.checkBox.isChecked = audioList[position].isSelected
        holder.bind(position)
    }


    fun updateItemList(audioList: List<FilesEntity>) {
        this.audioList = audioList
        notifyDataSetChanged()
    }
}