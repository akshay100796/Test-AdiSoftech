package com.codexdroid.sa_assingment.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.codexdroid.sa_assingment.R
import com.codexdroid.sa_assingment.controller.interfaces.ToDestination
import com.codexdroid.sa_assingment.databinding.ActivityHomeBinding
import com.codexdroid.sa_assingment.models.locals.ShopDetails
import com.codexdroid.sa_assingment.utils.AppConstants
import com.google.firebase.FirebaseApp

class HomeActivity : AppCompatActivity() , ToDestination{

    private lateinit var binding : ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_home)

        FirebaseApp.initializeApp(this)
        binding.idBottomNavView.setupWithNavController(findNavController(R.id.id_navHostFragment))
    }

    override fun navigateToDestination(id: Int?, data: Any?) {
        val shopsData = data as ShopDetails
        Bundle().apply {
            putSerializable(AppConstants.SHOPS_DATA,shopsData)
            findNavController(R.id.id_navHostFragment).navigate(R.id.action_id_nearby_fragment_to_shopDetailsFragment,this)
        }
    }
}