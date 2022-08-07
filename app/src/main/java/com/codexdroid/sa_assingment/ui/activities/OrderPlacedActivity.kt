package com.codexdroid.sa_assingment.ui.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.codexdroid.sa_assingment.R
import com.codexdroid.sa_assingment.databinding.ActivityOrderPlacedBinding
import com.codexdroid.sa_assingment.models.locals.ShopDetails
import com.codexdroid.sa_assingment.models.locals.ShopItem
import com.codexdroid.sa_assingment.models.room.ViewModelFactory
import com.codexdroid.sa_assingment.models.room.ViewModels
import com.codexdroid.sa_assingment.utils.AppConstants
import com.codexdroid.sa_assingment.utils.PrefManager
import com.codexdroid.sa_assingment.utils.toJson
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class OrderPlacedActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityOrderPlacedBinding
    private var shopDetails: ShopDetails? = null
    private var shopItem: ShopItem? = null
    private lateinit var prefManager: PrefManager
    private lateinit var viewModel: ViewModels

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_order_placed)

        initialisation()

        setOnClickedListener()
    }

    private fun initialisation () {
        prefManager = PrefManager(this)
        viewModel = ViewModelProvider(this, ViewModelFactory(application))[ViewModels::class.java]
        shopDetails = intent.getSerializableExtra(AppConstants.SHOPS_DATA) as ShopDetails
        shopItem = intent.getSerializableExtra(AppConstants.ITEM_DATA) as ShopItem

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setData()
    }

    private fun setData () {
        binding.idMobileName.text = "${shopItem?.item_name}  |  $${shopItem?.price}"

        val status = when(shopItem?.retailer_status) {
            -1 -> "Pending"
            0 -> "Rejected"
            1 -> "Approved"
            else -> "Not Yet"
        }
        binding.idApprovedStatus.text = "Approved Status, $status"

        Glide.with(binding.idImageMobile.context)
            .load(shopItem?.item_image)
            .into(binding.idImageMobile)

        Glide.with(binding.idImageQrCode.context)
            .load(shopDetails?.image_qr_code)
            .into(binding.idImageQrCode)
    }

    private fun setOnClickedListener () {

        binding.idButtonPlacedOrder.setOnClickListener {
            //todo --> update values in firebase
            viewModel.updateIsOrderPlaced(1,shopItem?.item_id!!)
            Toast.makeText(this,"Order Placed Successfully",Toast.LENGTH_LONG).show()
            finish()
        }

        binding.idButtonCancel.setOnClickListener {
            //todo --> update values in database isOrderPlaced = 0
            viewModel.updateIsOrderPlaced(0,shopItem?.item_id!!)
            Toast.makeText(this,"Order Cancelled",Toast.LENGTH_LONG).show()
            finish()
        }

        binding.idFabNavigation.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:${shopDetails?.latitude},${shopDetails?.longitude}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val shopName = LatLng(shopDetails?.latitude!!, shopDetails?.longitude!!)
        mMap.addMarker(MarkerOptions()
            .position(shopName)
            .title(shopDetails?.shop_name))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(shopName,16f))
    }
}