package com.codexdroid.sa_assingment.controller.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codexdroid.sa_assingment.controller.interfaces.RecyclerClickedListener
import com.codexdroid.sa_assingment.databinding.RecyclerViewShopItemsBinding
import com.codexdroid.sa_assingment.models.locals.LatLong
import com.codexdroid.sa_assingment.models.locals.ShopDetails
import com.codexdroid.sa_assingment.models.locals.ShopItem
import com.codexdroid.sa_assingment.utils.getDistanceInKm
import com.codexdroid.sa_assingment.utils.getTimeDurationFromMilliSec


class MobilesAdapter(private var shopList: ArrayList<ShopItem>, private var distance: String)
    : RecyclerView.Adapter<MobilesAdapter.ViewHolder>() {

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
                .load(shopList[position].item_image)
                .into(this.idImageView)

            this.idMobileName.text = shopList[position].item_name
            this.idTextAmountStatus.text = "Price : $${shopList[position].price}"
            this.idTextDuration.text =  "$distance Away from you"

            this.idContainer.setOnClickListener {
                listener.onRecyclerClicked(0,position,shopList[position],null)
            }
        }
    }

    override fun getItemCount(): Int = shopList.size
}