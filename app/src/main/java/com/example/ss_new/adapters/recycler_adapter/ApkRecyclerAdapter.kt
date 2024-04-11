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

class ApkRecyclerAdapter(
    var apkList: List<FilesEntity>,
    var context: Context, private var listener: FileSelectionListener
)  : RecyclerView.Adapter<ApkRecyclerAdapter.DocsViewHolder>() {

    inner class DocsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val checkBox: CheckBox = itemView.findViewById(R.id.chkSelected)
        fun bind(position: Int){
            itemView.findViewById<TextView>(R.id.itemAudioName).text = File(apkList[position].path).name
            itemView.findViewById<ImageView>(R.id.itemImgAudio).setImageResource(R.drawable.icon_apk_list)
            itemView.setOnClickListener {
                if(!checkBox.isChecked){
                    checkBox.isChecked = true
                    listener.selected(true,apkList[position].path)
                }else{
                    checkBox.isChecked = false
                    listener.selected(false,apkList[position].path)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocsViewHolder {
        return DocsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_audios, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return apkList.size
    }

    override fun onBindViewHolder(holder: DocsViewHolder, position: Int) {
        holder.checkBox.setOnClickListener {
            if(holder.checkBox.isChecked){
                listener.selected(true,apkList[position].path)
            }else{
                listener.selected(false,apkList[position].path)
            }
        }
        holder.checkBox.isChecked = apkList[position].isSelected
        holder.bind(position)
    }

    fun updateItemList(apkList: List<FilesEntity>) {
        this.apkList = apkList
        notifyDataSetChanged()
    }
}