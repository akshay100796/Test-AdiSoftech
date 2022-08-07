package com.codexdroid.sa_assingment.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.codexdroid.sa_assingment.R
import com.codexdroid.sa_assingment.databinding.ActivityEditProfileBinding
import com.codexdroid.sa_assingment.models.locals.UserRegisterData
import com.codexdroid.sa_assingment.utils.PrefManager
import com.codexdroid.sa_assingment.utils.getDateOnlyForFilter
import com.google.android.material.datepicker.MaterialDatePicker

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivityEditProfileBinding
    private lateinit var prefManager: PrefManager
    private lateinit var birthDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_edit_profile)

        prefManager = PrefManager(this)
        val userData = prefManager.loadUserRegisterData()

        binding.idEditMobile.setText(userData?.mobile)
        binding.idEditName.setText(userData?.name)
        binding.idTextBirthDate.text = userData?.date
        binding.idEditCompleteAddress.setText(userData?.address)
        binding.idEditCity.setText(userData?.city)
        binding.idEditLatLong.setText(userData?.latLong)
        binding.idEditProfession.setText(userData?.profession)
        binding.idEditIncome.setText(userData?.income)
        binding.idEditEmail.setText(userData?.email)

        binding.idButtonRegister.setOnClickListener {

            val mobile = binding.idEditMobile.text.toString()
            val name = binding.idEditName.text.toString()
            val date = getDateOnlyForFilter(birthDate.toLong())
            val address = binding.idEditCompleteAddress.text.toString()
            val city = binding.idEditCity.text.toString()
            val latLong = binding.idEditLatLong.text.toString()
            val profession = binding.idEditProfession.text.toString()
            val income = binding.idEditIncome.text.toString()
            val emailID = binding.idEditEmail.toString()
            val currentTime = System.currentTimeMillis()

            val data = UserRegisterData(mobile = mobile, name = name, date = date,address = address, city = city, latLong = latLong,profession = profession,income = income, email = emailID, timeMilliSec = currentTime)
            prefManager.saveUserRegisterData(data)

            Toast.makeText(this,"OTP For Login : 123456", Toast.LENGTH_LONG).show()
            finish()
        }

        binding.idTextBirthDate.setOnClickListener {

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Birth Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .build()
            datePicker.show(supportFragmentManager,"DatePicker")
            datePicker.addOnPositiveButtonClickListener {
                birthDate = getDateOnlyForFilter(it)
                binding.idTextBirthDate.text = birthDate
            }
        }
    }
}