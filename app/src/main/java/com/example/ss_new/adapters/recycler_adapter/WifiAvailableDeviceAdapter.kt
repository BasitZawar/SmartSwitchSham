package com.example.ss_new.adapters.recycler_adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ss_new.app_utils.data_classes.my_interfaces.MyClickCallbackInterface
import com.example.ss_new.databinding.ItemWifiDirectBinding


class WifiAvailableDeviceAdapter(
    private val context: Context,
    private val mArrayList: ArrayList<String>,
    private val myClickCallbackInterface: MyClickCallbackInterface
) : RecyclerView.Adapter<WifiAvailableDeviceAdapter.MViewHolder>() {


    inner class MViewHolder(val binding: ItemWifiDirectBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MViewHolder {
        return MViewHolder(
            ItemWifiDirectBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MViewHolder, position: Int) {

        holder.binding.deviceName.text = mArrayList[position]
        holder.itemView.setOnClickListener {
            myClickCallbackInterface.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return mArrayList.size
    }
}