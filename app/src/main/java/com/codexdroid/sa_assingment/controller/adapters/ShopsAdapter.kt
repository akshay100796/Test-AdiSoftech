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
import com.codexdroid.sa_assingment.models.locals.ShopDetails
import com.codexdroid.sa_assingment.utils.getDistanceInKm
import com.codexdroid.sa_assingment.utils.getTimeDurationFromMilliSec

class ShopsAdapter(private var shopList: ArrayList<ShopDetails>,private var latLong: LatLong)
    : RecyclerView.Adapter<ShopsAdapter.ViewHolder>() {

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
                .load(shopList[position].image_url)
                .into(this.idImageView)

            this.idMobileName.text = shopList[position].shop_name
            //this.idTextAmountStatus.text = "${getDistanceInKm(shopList[position].latitude,shopList[position].longitude,latLong.latitude!!,latLong.longitude!!)} KM Away from you"
            this.idTextAmountStatus.visibility = View.GONE
            this.idDistance.text = "${getDistanceInKm(latLong.latitude!!,latLong.longitude!!,shopList[position].latitude,shopList[position].longitude)} KM Away from you"

            this.idTextDuration.visibility = View.VISIBLE
            this.idTextDuration.text =  "Last Visit : ${getTimeDurationFromMilliSec(shopList[position].last_visit)}"

            this.idContainer.setOnClickListener {
                listener.onRecyclerClicked(0,position,shopList[position],null)
            }

        }

    }

    override fun getItemCount(): Int = shopList.size
}