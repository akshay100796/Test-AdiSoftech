package com.codexdroid.sa_assingment.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.codexdroid.sa_assingment.databinding.ActivityMapsBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onMapReady(googleMap: GoogleMap) {}
}