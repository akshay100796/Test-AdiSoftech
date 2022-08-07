package com.codexdroid.sa_assingment.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.codexdroid.sa_assingment.R
import com.codexdroid.sa_assingment.databinding.FragmentProfileBinding
import com.codexdroid.sa_assingment.ui.activities.EditProfileActivity
import com.codexdroid.sa_assingment.ui.activities.LoginActivity
import com.codexdroid.sa_assingment.utils.PrefManager
import com.codexdroid.sa_assingment.utils.getTimeDurationFromMilliSec

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var prefManager: PrefManager


    private var launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        setUpdate()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_profile, container, false)

        initialisation()

        setOnClickedListener()

        setUpdate()

        return binding.root


    }

    private fun setOnClickedListener() {

        binding.idImageEdit.setOnClickListener {
            launcher.launch(Intent(requireContext(),EditProfileActivity::class.java))
        }

        binding.idImageLogout.setOnClickListener {

            AlertDialog.Builder(requireContext())
                .setTitle("Logout?")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes,Logout") {d,_ ->
                    d.dismiss()
                    prefManager.clearAll()
                    startActivity(Intent(requireActivity(),LoginActivity::class.java))
                    requireActivity().finish()
                }
                .setNegativeButton("No, Cancel") {d,_ ->
                    d.dismiss()
                }
                .show()
        }
    }

    private fun initialisation() {
        prefManager = PrefManager(requireContext())

    }

    private fun setUpdate() {

        prefManager.loadUserRegisterData().apply {
            binding.idTextMobile.text = this?.mobile
            binding.idTextDateOfBirth.text = this?.date
            binding.idTextBrief.text = this?.profession
            binding.idTextAmount.text = this?.income
            binding.idTextEmail.text = if(this?.email=="") "Not Provided" else this?.email
            binding.idTextLasUpdated.text = "Last Updated, ${getTimeDurationFromMilliSec(this?.timeMilliSec)}"
        }

    }
}