package com.codexdroid.sa_assingment.ui.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.codexdroid.sa_assingment.R
import com.codexdroid.sa_assingment.models.locals.UserLocationData
import com.codexdroid.sa_assingment.utils.AppConstants
import com.codexdroid.sa_assingment.utils.NetworkUtils
import com.codexdroid.sa_assingment.utils.PrefManager
import com.google.android.gms.location.*
import java.util.*

// --> https://www.figma.com/file/DxZGUeMsgp8lNuSuJtNayr/SA_Assingment?node-id=101%3A2
// -->


class StartActivity : AppCompatActivity() {

    private lateinit var prefManager: PrefManager
    private lateinit var locationManager: LocationManager
    private var currentLatitude : Double? = null
    private var currentLongitude : Double? = null
    private var isLocationTaken = false

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

    private var gpsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        checkGpsEnableOrNot()
    }

    private var requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        checkOrRequestPermission()

//        if(it[Manifest.permission.ACCESS_COARSE_LOCATION] == true && it[Manifest.permission.ACCESS_FINE_LOCATION] == true){
//            checkOrRequestPermission()
//        }else{
//            gotoHomeScreen()
//        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        initialisation()
    }

    private fun initialisation() {
        prefManager = PrefManager(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager  = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationRequest = LocationRequest.create().apply {
            interval = 10_000L
            fastestInterval = 5_000L
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }


        if(prefManager.getToken() != null) {
            gotoHomeScreen()
        } else checkGpsEnableOrNot()
    }

    private fun checkGpsEnableOrNot() {

        if(NetworkUtils.isConnected(this)) {
            try {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                    checkOrRequestPermission()
                else onProviderDisable()

            }catch (ex : Exception){
                ex.printStackTrace()
            }
        } else {
            val text = "No Internet Connection\nHint : Turn on Internet and Reopen App"
            Toast.makeText(this,text,Toast.LENGTH_LONG).show()
        }
    }

    private fun checkOrRequestPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


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

    private fun gotoLoginScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }, AppConstants.TIME_2_SEC)
    }

    private fun gotoHomeScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this,HomeActivity::class.java))
            finish()
        }, AppConstants.TIME_2_SEC)

    }

    private fun saveLocationAndContinue(location: Location?){

        if(location != null){
            try {

                location.apply {

                    val addressList = Geocoder(this@StartActivity, Locale.getDefault()).getFromLocation(location.latitude, location.longitude, 1)

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

                    val locationData = UserLocationData(countryName, adminArea, subAdminArea, locality, featureName, postalCode, addressLine, countryCode, latitude, longitude)
                    prefManager.saveUserLocationData(locationData)

                    gotoLoginScreen()
                }
            }catch (ex: Exception){
                Log.d("FATAL","$ex")
                ex.printStackTrace()
            }
        }else gotoLoginScreen()
    }

    private fun onProviderDisable() {
        AlertDialog.Builder(this)
            .setTitle("Enable Location")
            .setMessage("Please Enable Location to get near by retailers")
            .setCancelable(false)
            .setPositiveButton("Enable") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                gpsLauncher.launch(intent)
            }
            .setNegativeButton("Exit") {d,_ ->
                d.dismiss()
                finish()
            }
            .show()

    }
}
