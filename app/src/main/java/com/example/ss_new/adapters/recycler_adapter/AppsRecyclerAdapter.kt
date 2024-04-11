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
import com.example.ss_new.app_utils.AllFilesUtils
import com.example.ss_new.app_utils.FileSelectionListener

class AppsRecyclerAdapter(var appList: List<FilesEntity>, var context: Context, private var listener : FileSelectionListener)  : RecyclerView.Adapter<AppsRecyclerAdapter.AudioViewHolder>() {

    inner class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val checkBox: CheckBox = itemView.findViewById(R.id.chkSelected)
        fun bind(position: Int){
            val items  = AllFilesUtils.getAppDetails(context,appList[position].path)
            itemView.findViewById<TextView>(R.id.itemAudioName).text = items?.name
//            itemView.findViewById<TextView>(R.id.tvVideoSize).text = items?.size
            itemView.findViewById<ImageView>(R.id.itemImgAudio).setImageDrawable(items?.icon)
            itemView.setOnClickListener {
                if(!checkBox.isChecked){
                    checkBox.isChecked = true
                    listener.selected(true,appList[position].path)
                }else{
                    checkBox.isChecked = false
                    listener.selected(false,appList[position].path)
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
        return appList.size
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {

        holder.checkBox.setOnClickListener {
            if(holder.checkBox.isChecked){
                listener.selected(true,appList[position].path)
            }else{
                listener.selected(false,appList[position].path)
            }
        }
        holder.checkBox.isChecked = appList[position].isSelected
        holder.bind(position)
    }

    fun updateItemList(audioList: List<FilesEntity>) {
        this.appList = audioList
        notifyDataSetChanged()
    }
}