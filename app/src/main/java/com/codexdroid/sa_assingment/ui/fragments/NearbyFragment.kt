package com.codexdroid.sa_assingment.ui.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavHostController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.codexdroid.sa_assingment.R
import com.codexdroid.sa_assingment.controller.adapters.ShopsAdapter
import com.codexdroid.sa_assingment.controller.interfaces.RecyclerClickedListener
import com.codexdroid.sa_assingment.controller.interfaces.ToDestination
import com.codexdroid.sa_assingment.databinding.FragmentNearbyBinding
import com.codexdroid.sa_assingment.models.locals.LatLong
import com.codexdroid.sa_assingment.models.locals.ShopDetails
import com.codexdroid.sa_assingment.models.locals.UserLocationData
import com.codexdroid.sa_assingment.utils.*
import com.google.android.gms.location.*
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

class NearbyFragment : Fragment() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var prefManager: PrefManager
    private lateinit var binding: FragmentNearbyBinding
    private var shopsList : ArrayList<ShopDetails>? = null
    private var locationData : UserLocationData? = null

    private lateinit var locationManager: LocationManager
    private var currentLatitude : Double? = null
    private var currentLongitude : Double? = null
    private var isLocationTaken = false

    private lateinit var toDestination: ToDestination

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationRequest: LocationRequest

    private var locationCallback = object : LocationCallback() {

        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)

            val location = result.locations[0]
            if(location != null) {
                if(!isLocationTaken) {
                    isLocationTaken = true
                    currentLatitude = result.locations[0]?.latitude
                    currentLongitude = result.locations[0]?.longitude
                    saveLocationAndContinue(location)
                }
            }
        }
    }

    private var requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        checkOrRequestPermission()
    }


    private var gpsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        checkGpsEnableOrNot()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_nearby, container, false)

        initialisation()

        return binding.root
    }

    private fun initialisation() {

        prefManager = PrefManager(requireContext())
        toDestination = activity as ToDestination

        databaseReference = FirebaseDatabase.getInstance("https://adisoftech-assignment-default-rtdb.firebaseio.com/").getReference(AppConstants.REFERENCE_SHOPS)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationManager  = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationRequest = LocationRequest.create().apply {
            interval = 10_000L
            fastestInterval = 5_000L
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        checkGpsEnableOrNot()
    }


    private fun checkGpsEnableOrNot() {

        if(NetworkUtils.isConnected(requireContext())) {
            try {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                    checkOrRequestPermission()
                else onProviderDisable()

            }catch (ex : Exception){
                ex.printStackTrace()
            }
        } else {
            val text = "No Internet Connection\nHint : Turn on Internet and Reopen App"
            Toast.makeText(requireContext(),text, Toast.LENGTH_LONG).show()
        }
    }

    private fun checkOrRequestPermission() {

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            try {
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                if(location != null){
                    saveLocationAndContinue(location)
                }else{

                    fusedLocationClient?.lastLocation?.addOnSuccessListener { loc ->
                        if(loc != null) {
                            saveLocationAndContinue(loc)
                        }else {
                            fusedLocationClient?.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())
                            saveLocationAndContinue(null)
                        }
                    }
                }

            }catch (ex: Exception){
                ex.printStackTrace()
            }
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    private fun saveLocationAndContinue(location: Location?){

        try {

            location.apply {

                val addressList = Geocoder(
                    requireContext(),
                    Locale.getDefault()
                ).getFromLocation(location!!.latitude, location.longitude, 1)

                val countryName: String = addressList[0].countryName ?: ""
                val adminArea: String = addressList[0].adminArea ?: ""
                val locality: String = addressList[0].locality
                val postalCode: Int = addressList[0].postalCode.toInt()
                val addressLine: String = addressList[0].getAddressLine(0)
                val countryCode: String = addressList[0].countryCode ?: ""
                val featureName: String = addressList[0].featureName ?: ""
                val subAdminArea: String = addressList[0].subAdminArea ?: ""
                val latitude: Double = location.latitude
                val longitude: Double = location.longitude

                locationData = UserLocationData(countryName, adminArea, subAdminArea, locality, featureName, postalCode, addressLine, countryCode, latitude, longitude)
                prefManager.saveUserLocationData(locationData)

                loadDataFromFirebase()
            }

        } catch (ex: Exception) {
            Log.d("FATAL", "$ex")
            ex.printStackTrace()
        }
    }

    private fun onProviderDisable() {
        AlertDialog.Builder(requireContext())
            .setTitle("Enable Location")
            .setMessage("Please Enable Location to get near by retailers")
            .setCancelable(false)
            .setPositiveButton("Enable") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                gpsLauncher.launch(intent)
            }
            .setNegativeButton("Cancel") {d,_ ->
                d.dismiss()
            }
            .show()

    }

    private fun loadDataFromFirebase () {
        shopsList = arrayListOf()


        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (shops in snapshot.children) {
                    shopsList?.add(shops.getValue(ShopDetails::class.java)!!)
                }
                setAdapter()
            }

            override fun onCancelled(error: DatabaseError) {
                error.toException().printStackTrace()
            }
        })
    }

    private fun setAdapter() {
        binding.idProgressBar.visibility = View.GONE
        binding.idRecyclerNearbyList.visibility = View.VISIBLE


        shopsList?.sortWith(DistanceComparator(LatLong(locationData?.latitude,locationData?.longitude)))
        //Collections.sort(shopsList!!, DistanceComparator( LatLong(locationData?.latitude,locationData?.longitude)))

        ShopsAdapter(shopsList!!, LatLong(locationData?.latitude,locationData?.longitude)).apply {
            binding.idRecyclerNearbyList.adapter = this

            setListener(object : RecyclerClickedListener{
                override fun onRecyclerClicked(id: Int?, position: Int?, data: Any?, extraText: String?) {
                    val shopsData = data as ShopDetails
                    //Update last visit shop
                    databaseReference.child(shopsData.shop_name).child("last_visit").setValue(System.currentTimeMillis())
                    toDestination.navigateToDestination(position,shopsData)
                }
            })
        }
    }
}