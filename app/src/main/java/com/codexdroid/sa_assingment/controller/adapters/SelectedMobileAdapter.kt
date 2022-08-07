package com.codexdroid.sa_assingment.controller.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codexdroid.sa_assingment.controller.interfaces.RecyclerClickedListener
import com.codexdroid.sa_assingment.databinding.RecyclerViewShopItemsBinding
import com.codexdroid.sa_assingment.models.locals.LatLong
import com.codexdroid.sa_assingment.models.room.TableMobile
import com.codexdroid.sa_assingment.utils.getDistanceInKm
import com.codexdroid.sa_assingment.utils.getTimeDurationFromMilliSec

class SelectedMobileAdapter(private var mobileData: ArrayList<TableMobile>, var latLong: LatLong, private var isForSelection: Boolean)
    : RecyclerView.Adapter<SelectedMobileAdapter.ViewHolder>() {

    private lateinit var listener: RecyclerClickedListener

    class ViewHolder(var binding: RecyclerViewShopItemsBinding) : RecyclerView.ViewHolder(binding.root)

    fun setListener(listener: RecyclerClickedListener){
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RecyclerViewShopItemsBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.apply {
            Glide.with(this.idImageView.context)
                .load(mobileData[position].mobileImage)
                .into(this.idImageView)

            this.idMobileName.text = mobileData[position].mobileName
            this.idTextAmountStatus.text = "Price : $${mobileData[position].mobilePrice}"
            this.idDistance.text =  "${getDistanceInKm(mobileData[position].shopLatitude?.toDouble()!!, 
                mobileData[position].shopLongitude?.toDouble()!!,latLong.latitude!!,latLong.longitude!!)} KM Away from you"

            this.idTextDuration.visibility = View.VISIBLE
            this.idTextDuration.text = if(isForSelection)
                "Selected, ${getTimeDurationFromMilliSec(mobileData[position].currentTimeMilliSec?.toLong())}"
            else "Order Placed, ${getTimeDurationFromMilliSec(mobileData[position].currentTimeMilliSec?.toLong())}"

            this.idContainer.setOnClickListener {
                listener.onRecyclerClicked(0,position,mobileData[position],null)
            }
        }
    }

    override fun getItemCount(): Int = mobileData.size
}
