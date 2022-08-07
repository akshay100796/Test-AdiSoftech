package com.codexdroid.sa_assingment.ui.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.codexdroid.sa_assingment.R
import com.codexdroid.sa_assingment.controller.adapters.MobilesAdapter
import com.codexdroid.sa_assingment.controller.interfaces.RecyclerClickedListener
import com.codexdroid.sa_assingment.databinding.FragmentShopDetailsBinding
import com.codexdroid.sa_assingment.databinding.LayoutDialogViewProductBinding
import com.codexdroid.sa_assingment.models.locals.ShopDetails
import com.codexdroid.sa_assingment.models.locals.ShopItem
import com.codexdroid.sa_assingment.models.room.TableMobile
import com.codexdroid.sa_assingment.models.room.ViewModelFactory
import com.codexdroid.sa_assingment.models.room.ViewModels
import com.codexdroid.sa_assingment.ui.activities.OrderPlacedActivity
import com.codexdroid.sa_assingment.utils.AppConstants
import com.codexdroid.sa_assingment.utils.PrefManager
import com.codexdroid.sa_assingment.utils.getDistanceInKm
import com.codexdroid.sa_assingment.utils.toJson


class ShopDetailsFragment : Fragment() {

    private lateinit var binding: FragmentShopDetailsBinding
    private var shopDetails : ShopDetails? = null
    private lateinit var prefManager: PrefManager

    private lateinit var viewModel: ViewModels

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_shop_details, container, false)
        initialisation()

        return binding.root
    }

    private fun initialisation () {

        prefManager = PrefManager(requireContext())
        viewModel = ViewModelProvider(this, ViewModelFactory(requireActivity().application))[ViewModels::class.java]

        shopDetails = requireArguments().getSerializable(AppConstants.SHOPS_DATA) as ShopDetails
        binding.idShopName.text = shopDetails?.shop_name

        val userLocationData = prefManager.loadUserLocationData()
        val distance = getDistanceInKm(shopDetails?.latitude!!,shopDetails?.longitude!!,userLocationData?.latitude!!,userLocationData.longitude)
        MobilesAdapter(shopDetails?.shop_items!!,distance).apply {
            binding.idRecyclerShop.adapter = this

            this.setListener(object : RecyclerClickedListener{

                override fun onRecyclerClicked(id: Int?, position: Int?, data: Any?, extraText: String?) {
                    val shopItem = data as ShopItem
                    openDialog(shopItem,distance)

                    val tableMobile = TableMobile(
                        mobileId = shopItem.item_id.toString(),
                        mobileName = shopItem.item_name,
                        mobileImage =  shopItem.item_image,
                        mobilePrice = shopItem.price.toString(),
                        mobileOrderStatus = shopItem.retailer_status.toString(),
                        shopId = shopDetails?.shop_id.toString(),
                        shopName = shopDetails?.shop_name,
                        shopImageUrl = shopDetails?.image_url,
                        shopImageQR_Code = shopDetails?.image_qr_code,
                        shopLatitude = shopDetails?.latitude.toString(),
                        shopLongitude = shopDetails?.longitude.toString(),
                        lastVisit = shopDetails?.last_visit.toString(),
                        currentTimeMilliSec = System.currentTimeMillis().toString(), isSelected = 1)
                    viewModel.saveMobileData(tableMobile)

                }

            })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun openDialog(shopItem: ShopItem, distance: String) {

        val dialog = AlertDialog.Builder(requireContext())
        val customView = layoutInflater.inflate(R.layout.layout_dialog_view_product,null)
        val binding = DataBindingUtil.bind<LayoutDialogViewProductBinding>(customView?.rootView!!)
        dialog.setView(binding?.root)

        val customDialog = dialog.create()
        customDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding?.apply {
            Glide.with(this.idImageView.context)
                .load(shopItem.item_image)
                .into(this.idImageView)

            this.idMobileName.text  = shopItem.item_name
            this.idPrice.text = "Price : $${shopItem.price}"
            this.idDistance.text = "$distance KM Far From You"

            this.idButtonNext.setOnClickListener {
                customDialog.dismiss()
                Intent(requireActivity(),OrderPlacedActivity::class.java).apply {
                    putExtra(AppConstants.SHOPS_DATA,shopDetails)
                    putExtra(AppConstants.ITEM_DATA,shopItem)
                    startActivity(this)
                }
            }
            this.idButtonCancel.setOnClickListener {
                customDialog.dismiss()
            }
        }
        customDialog?.show()

    }
}