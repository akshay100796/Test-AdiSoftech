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
import androidx.databinding.DataBindingUtil
import com.codexdroid.sa_assingment.R
import com.codexdroid.sa_assingment.databinding.ActivityRegistrationBinding
import com.codexdroid.sa_assingment.models.locals.UserLocationData
import com.codexdroid.sa_assingment.models.locals.UserRegisterData
import com.codexdroid.sa_assingment.utils.AppConstants
import com.codexdroid.sa_assingment.utils.NetworkUtils
import com.codexdroid.sa_assingment.utils.PrefManager
import com.codexdroid.sa_assingment.utils.getDateOnlyForFilter
import com.google.android.gms.location.*
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class RegistrationActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var binding: ActivityRegistrationBinding
    private var birthDate: Long? = 0L
    private lateinit var prefManager: PrefManager
    private lateinit var userLocationData: UserLocationData

    private lateinit var locationManager: LocationManager
    private var currentLatitude : Double? = null
    private var currentLongitude : Double? = null
    private var isLocationTaken = false
    private var mobile: String? = null

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_registration)
        prefManager = PrefManager(this)

        initialisation()
        setOnClickedListener()
    }

    private fun initialisation() {

        databaseReference = FirebaseDatabase.getInstance("https://adisoftech-assignment-default-rtdb.firebaseio.com/").getReference(AppConstants.REFERENCE_USERS)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager  = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mobile = intent.getStringExtra(AppConstants.MOBILE)

        if(mobile != null) {
            binding.idEditMobile.setText(mobile)
        }

        locationRequest = LocationRequest.create().apply {
            interval = 10_000L
            fastestInterval = 5_000L
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        checkGpsEnableOrNot()
    }

    private fun setOnClickedListener () {

        binding.idButtonRegister.setOnClickListener {

            val mobile = binding.idEditMobile.text.toString()
            val name = binding.idEditName.text.toString()
            val date = getDateOnlyForFilter(birthDate)
            val address = binding.idEditCompleteAddress.text.toString()
            val city = binding.idEditCity.text.toString()
            val latLong = binding.idEditLatLong.text.toString()
            val profession = binding.idEditProfession.text.toString()
            val income = binding.idEditIncome.text.toString()
            val emailID = binding.idEditEmail.text.toString()
            val currentTime = System.currentTimeMillis()

            val data = UserRegisterData(
                mobile = mobile,
                name = name,
                date = date,
                address = address,
                city = city,
                latLong = latLong,
                profession = profession,
                income = income,
                email = emailID,
                timeMilliSec = currentTime)

            prefManager.saveUserRegisterData(data)

            databaseReference.child(mobile).setValue(data)
                .addOnSuccessListener {
                    Toast.makeText(this,"OTP For Login : 123456",Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this,"Q. Whats Wrong?, \nA. ${it.message}",Toast.LENGTH_LONG).show()
                }
        }
        binding.idTextBirthDate.setOnClickListener {

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Birth Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .build()
            datePicker.show(supportFragmentManager,"DatePicker")
            datePicker.addOnPositiveButtonClickListener {
                birthDate = it
                binding.idTextBirthDate.text = getDateOnlyForFilter(birthDate)
            }
        }


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

    private fun saveLocationAndContinue(location: Location?){
        try {

            location.apply {

                val addressList = Geocoder(this@RegistrationActivity, Locale.getDefault()).getFromLocation(location?.latitude!!, location.longitude, 1)

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

                userLocationData = UserLocationData(countryName, adminArea, subAdminArea, locality, featureName, postalCode, addressLine, countryCode, latitude, longitude)
                prefManager.saveUserLocationData(userLocationData)

                binding.idEditCompleteAddress.setText(addressLine)
                binding.idEditCity.setText(adminArea)
                binding.idEditLatLong.setText("${latitude},${longitude}")
            }
        }catch (ex: Exception){
            Log.d("FATAL","$ex")
            ex.printStackTrace()
        }
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