package com.example.ss_new.adapters.recycler_adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
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

class ImageRecyclerAdapter(
    var imageList: List<FilesEntity>,
   var context: Context, private val listener: FileSelectionListener
) : RecyclerView.Adapter<ImageRecyclerAdapter.ImageViewHolder>() {
    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val checkBox: CheckBox = itemView.findViewById(R.id.chkSelected)
        fun bind(position: Int){
            itemView.findViewById<TextView>(R.id.tvVideoName).text = File(imageList[position].path).name
            itemView.findViewById<TextView>(R.id.tvVideoSize).text = AllFilesUtils.changeSizeToFormat(File(imageList[position].path).length())
            Glide.with(context).load(imageList[position].path).transform(CenterCrop(),RoundedCorners(1))
                .placeholder(R.drawable.icon_img_list).into(itemView.findViewById(R.id.imgVideoThumb))

            itemView.setOnClickListener {
                if(!checkBox.isChecked){
                    checkBox.isChecked = true
                    listener.selected(true,imageList[position].path)
                }else{
                    checkBox.isChecked = false
                    listener.selected(false,imageList[position].path)
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {

            return ImageViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_image_video, parent, false)
            )
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {


        holder.checkBox.setOnClickListener {
            if(holder.checkBox.isChecked){
                listener.selected(true,imageList[position].path)
            }else{
                listener.selected(false,imageList[position].path)
            }
        }
        holder.checkBox.isChecked = imageList[position].isSelected
        holder.bind(position)
    }

    fun updateItemList(imageList: List<FilesEntity>) {
        this.imageList = imageList
        notifyDataSetChanged()
    }
}