package com.example.ss_new.adapters.recycler_adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.ss_new.R
import com.example.ss_new.database.FilesEntity
import com.example.ss_new.app_utils.AllFilesUtils
import com.example.ss_new.app_utils.FileSelectionListener
import java.io.File

class VideoRecyclerAdapter(
    var videoList: List<FilesEntity>,
    var context:Context, var listener: FileSelectionListener
) : RecyclerView.Adapter<VideoRecyclerAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val checkBox: CheckBox = itemView.findViewById(R.id.chkSelected)
        fun bind(position: Int){
            itemView.findViewById<TextView>(R.id.tvVideoName).text = File(videoList[position].path).name
            itemView.findViewById<TextView>(R.id.tvVideoSize).text = AllFilesUtils.changeSizeToFormat(File(videoList[position].path).length())
            Glide.with(context).load(videoList[position].path)
                .transform(CenterCrop(),RoundedCorners(1))
                .placeholder(R.drawable.baseline_error_24).into(itemView.findViewById(R.id.imgVideoThumb))
            itemView.findViewById<ImageView>(R.id.imgVideos).visibility = View.VISIBLE

            itemView.setOnClickListener {
                if(!checkBox.isChecked){
                    checkBox.isChecked = true
                    listener.selected(true,videoList[position].path)
                }else{
                    checkBox.isChecked = false
                    listener.selected(false,videoList[position].path)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
            return VideoViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_image_video, parent, false)
            )
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {


        holder.checkBox.setOnClickListener {
            if(holder.checkBox.isChecked){
                listener.selected(true,videoList[position].path)
            }else{
                listener.selected(false,videoList[position].path)
            }
        }
        holder.checkBox.isChecked = videoList[position].isSelected
        holder.bind(position)
    }

    fun updateItemList(videoList: List<FilesEntity>) {
        this.videoList = videoList
        notifyDataSetChanged()
    }
}