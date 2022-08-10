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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.codexdroid.sa_assingment.R
import com.codexdroid.sa_assingment.controller.adapters.SelectedMobileAdapter
import com.codexdroid.sa_assingment.controller.interfaces.RecyclerClickedListener
import com.codexdroid.sa_assingment.databinding.FragmentHomeBinding
import com.codexdroid.sa_assingment.databinding.LayoutDialogViewProductBinding
import com.codexdroid.sa_assingment.models.locals.*
import com.codexdroid.sa_assingment.models.room.TableMobile
import com.codexdroid.sa_assingment.models.room.ViewModelFactory
import com.codexdroid.sa_assingment.models.room.ViewModels
import com.codexdroid.sa_assingment.ui.activities.OrderPlacedActivity
import com.codexdroid.sa_assingment.utils.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: ViewModels
    private lateinit var prefManager: PrefManager
    private lateinit var userLocationData: UserLocationData

    private var userData: UserRegisterData? = null
    private var tableMobile : ArrayList<TableMobile>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_home, container, false)
        viewModel = ViewModelProvider(this, ViewModelFactory(requireActivity().application))[ViewModels::class.java]

        initialisation()

        setOnClickedListener()

        return binding.root
    }

    private fun initialisation() {
        prefManager = PrefManager(requireContext())
        try {
            userLocationData = prefManager.loadUserLocationData()!!
        }catch (ex: Exception) {}

        userData = prefManager.loadUserRegisterData()

        binding.idTextName.text = "Hi...!, ${userData?.name}"

        lifecycleScope.launch {
            loadMobileData()
                //.onStart {  }
                //.onCompletion {  }
                .catch {
                    manageList(false,"something wrong...!")
                }
                .collect {
                    tableMobile = it as ArrayList<TableMobile>
                    if(tableMobile != null && tableMobile?.size!! > 0) {
                        manageList(true,"")
                        tableMobile?.sortBy { mobileData ->
                            getDistance(LatLong(mobileData.shopLatitude?.toDouble(),mobileData.shopLongitude?.toDouble()))
                        }

                        setAdapter(tableMobile!!,true)
                    } else {
                        manageList(false,"No Data Found")
                    }
                }
        }
    }

    private fun getDistance(location: LatLong): String = getDistanceInKm(location.latitude!!,location.longitude!!,userLocationData.latitude,userLocationData.longitude)

    private fun manageList(isDataAvailable: Boolean,message: String) {
        if(isDataAvailable) {
            binding.idRecyclerList.visibility = View.VISIBLE
            binding.idTextNoData.visibility = View.GONE
        } else {
            binding.idRecyclerList.visibility = View.GONE
            binding.idTextNoData.visibility = View.VISIBLE
            binding.idTextNoData.text = message
        }
    }


    private fun loadMobileData() : Flow<List<TableMobile>> = flow {
        emitAll(viewModel.loadMobileData())
    }

    private fun setOnClickedListener() {

        binding.idChipAll.setOnClickListener {
            try {
                if(tableMobile != null && tableMobile?.isNotEmpty() == true) {
                    Collections.sort(tableMobile!!, SelectedDeviceComparator( LatLong(userLocationData.latitude,userLocationData.longitude)))
                    setAdapter(tableMobile!!,true)
                    manageList(true,"")
                } else {
                    manageList(false,"No Data Found")
                }

            }catch (ex: Exception) {}

        }

        binding.idChipSelected.setOnClickListener {
            try {
                val list = tableMobile?.filter {
                    it.isSelected == 1 && it.isOrderPlaced == 0
                }

                if(list != null && list.isNotEmpty()) {
                    Collections.sort(tableMobile!!, SelectedDeviceComparator( LatLong(userLocationData.latitude,userLocationData.longitude)))
                    manageList(true,"")
                    setAdapter(list as ArrayList<TableMobile>,true)
                } else {
                    manageList(false,"No Selected Device Found")
                }

            }catch (ex: Exception){
            }
        }

        binding.idChipPlaced.setOnClickListener {
            try {

                if(tableMobile != null && tableMobile?.isNotEmpty() == true) {

                    val list = tableMobile?.filter { it.isOrderPlaced == 1 } as ArrayList<TableMobile>
                    Collections.sort(tableMobile!!, SelectedDeviceComparator( LatLong(userLocationData.latitude,userLocationData.longitude)))

                    if(list.isNotEmpty()) {
                        manageList(true,"")
                        setAdapter(list,false)
                    } else manageList(false,"Not placed an Order")
                }

            }catch (ex: Exception){ }
        }
    }

    private fun setAdapter(tableMobile: ArrayList<TableMobile>,isForSelected : Boolean) {

        SelectedMobileAdapter(tableMobile, LatLong(userLocationData.latitude,userLocationData.longitude),isForSelected).apply {
            binding.idRecyclerList.adapter = this
            setListener(object : RecyclerClickedListener{
                override fun onRecyclerClicked(id: Int?, position: Int?, data: Any?, extraText: String?) {
                    val mobileData = data as TableMobile

                    if(mobileData.isOrderPlaced == 0) {
                        //show dialog,
                        //update value,
                        //open order details screen
                        openOrderDialog(mobileData)
                    } else {
                        //open order details screen
                        AlertDialog.Builder(requireContext())
                            .setTitle("Order Already Placed")
                            .setMessage("Do You Want to Re-Placed this Order?")
                            .setPositiveButton("YES") { d,_ ->
                                d.dismiss()
                                gotoNextScreen(mobileData)
                            }
                            .setNegativeButton("NO") {d,_->
                                d.dismiss()
                            }
                            .show()
                    }
                }
            })
        }
    }



    @SuppressLint("SetTextI18n")
    private fun openOrderDialog(mobileData: TableMobile) {

        val dialog = AlertDialog.Builder(requireContext())
        val customView = layoutInflater.inflate(R.layout.layout_dialog_view_product,null)
        val binding = DataBindingUtil.bind<LayoutDialogViewProductBinding>(customView?.rootView!!)
        dialog.setView(binding?.root)

        val customDialog = dialog.create()
        customDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding?.apply {
            Glide.with(this.idImageView.context)
                .load(mobileData.mobileImage)
                .into(this.idImageView)

            this.idMobileName.text  = mobileData.mobileName
            this.idPrice.text = "Price : $${mobileData.mobilePrice}"
            this.idDistance.text = "$${getDistance(LatLong(userLocationData.latitude,userLocationData.longitude))} KM Far From You"
            this.idButtonNext.text = "Placed Order"

//            val shopItem = ShopItem(
//                mobileData.mobileId.toInt(),mobileData.mobileImage!!,
//                item_name = mobileData.mobileName!!,
//                price = mobileData.mobilePrice?.toDouble()!!,2)
//
//            val shopDetails = ShopDetails(
//                image_qr_code = mobileData.shopImageQR_Code!!,
//                image_url = mobileData.shopImageUrl!!,
//                last_visit = mobileData.lastVisit?.toLong()!!,
//                latitude = mobileData.shopLatitude?.toDouble()!!,
//                longitude = mobileData.shopLongitude?.toDouble()!!,
//                shop_id = mobileData.shopId?.toInt()!!,
//                shop_name = mobileData.shopName!!,
//                shop_items = arrayListOf(shopItem))


            this.idButtonNext.setOnClickListener {
                customDialog.dismiss()
                gotoNextScreen(mobileData)
            }
            this.idButtonCancel.setOnClickListener {
                customDialog.dismiss()
            }
        }
        customDialog?.show()
    }

    private fun gotoNextScreen(mobileData: TableMobile) {
        val shopItem = ShopItem(
            mobileData.mobileId.toInt(),mobileData.mobileImage!!,
            item_name = mobileData.mobileName!!,
            price = mobileData.mobilePrice?.toDouble()!!,2)

        val shopDetails = ShopDetails(
            image_qr_code = mobileData.shopImageQR_Code!!,
            image_url = mobileData.shopImageUrl!!,
            last_visit = mobileData.lastVisit?.toLong()!!,
            latitude = mobileData.shopLatitude?.toDouble()!!,
            longitude = mobileData.shopLongitude?.toDouble()!!,
            shop_id = mobileData.shopId?.toInt()!!,
            shop_name = mobileData.shopName!!,
            shop_items = arrayListOf(shopItem))

        Intent(requireActivity(), OrderPlacedActivity::class.java).apply {
            putExtra(AppConstants.SHOPS_DATA,shopDetails)
            putExtra(AppConstants.ITEM_DATA,shopItem)
            startActivity(this)
        }

    }
}