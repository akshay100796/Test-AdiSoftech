package com.codexdroid.sa_assingment.ui.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import com.codexdroid.sa_assingment.R
import com.codexdroid.sa_assingment.controller.viewmodels.CommonViewModels
import com.codexdroid.sa_assingment.databinding.ActivityLoginBinding
import com.codexdroid.sa_assingment.models.locals.UserRegisterData
import com.codexdroid.sa_assingment.utils.AppConstants
import com.codexdroid.sa_assingment.utils.PrefManager
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.*
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefManager: PrefManager
    private lateinit var countDownTimer: CountDownTimer
    private var serverOtp : String = "123456"
    private lateinit var auth: FirebaseAuth


    private lateinit var clientOtp: String
    var userData: UserRegisterData? = null

    private val commonViewModel : CommonViewModels by viewModels()

    private var registerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        setTitleText("","")
    }


    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {}

        override fun onVerificationFailed(e: FirebaseException) {
            e.printStackTrace()
            AlertDialog.Builder(this@LoginActivity)
                .setTitle("Testing Credentials")
                .setMessage("${e.message} \n\nMobile : 7757888063, OTP : 123456")
                .setPositiveButton("Ok"){d,_ ->
                    d.dismiss()
                }
                .show()
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            serverOtp = verificationId
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_login)

        initialisation()

        setOnClickListener()

        setObserver()
    }

    private fun initialisation() {
        prefManager = PrefManager(this)
        databaseReference = FirebaseDatabase.getInstance("https://adisoftech-assignment-default-rtdb.firebaseio.com/").getReference(AppConstants.REFERENCE_USERS)

        auth = FirebaseAuth.getInstance()
        auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
    }

    private fun setOnClickListener() {

        binding.idEditMobile.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                if(charSequence?.length == 10) {
                    binding.idTextRequestOtp.apply {
                        isEnabled = true
                        setTextColor(ResourcesCompat.getColor(resources,R.color.color_primary,null))
                        resetView()
                    }
                } else {
                    binding.idTextRequestOtp.apply {
                        isEnabled = false
                        setTextColor(ResourcesCompat.getColor(resources,R.color.color_secondary,null))
                        resetView()
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.idInputOtp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                binding.idButtonLogin.isEnabled = charSequence?.length == 6
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.idTextRequestOtp.setOnClickListener {

            setTitleText("Checking","Mobile Number")
            val mobile = binding.idEditMobile.text.toString()

            databaseReference.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snap in snapshot.children) {

                        if(snap.key == mobile) {
                            userData = snap.getValue(UserRegisterData::class.java)
                        }
                    }

                    if (userData != null) {
                        //Mobile Found, visible element and take otp from server
                        setTitleText("","")
                        startCountDownTimer()
                        binding.idButtonLogin.visibility = View.VISIBLE
                        binding.idInputOtp.visibility = View.VISIBLE
                        binding.idInputOtp.isEnabled = true

                        if ( mobile == "7757888063") { //pre-define
                            Toast.makeText(this@LoginActivity,"Test Number OTP : 123456",Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@LoginActivity,"OTP Sent",Toast.LENGTH_LONG).show()
                            val options = PhoneAuthOptions.newBuilder()
                                .setPhoneNumber("+91${mobile}")
                                .setTimeout(30L, TimeUnit.SECONDS)
                                .setActivity(this@LoginActivity)
                                .setCallbacks(callbacks)
                                .build()
                            PhoneAuthProvider.verifyPhoneNumber(options)
                        }

                    } else {
                        //Mobile No Not Found navigate to register screen
                        toRegister(mobile)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        binding.idButtonLogin.setOnClickListener {
            clientOtp = binding.idInputOtp.text.toString()

            if(clientOtp != serverOtp) {
                setTitleText("Invalid OTP","Please Enter Valid OTP")
                return@setOnClickListener
            }
            prefManager.saveToken("CQGLLeDakgpZ5qwDSMEfpZl+SSELcxJcX9u78YLgJr4=") //Encryption Text Of "Hello There"
            prefManager.saveUserRegisterData(userData)
            startActivity(Intent(this,HomeActivity::class.java))
            finish()
        }

        binding.idImagePlusCircle.setOnClickListener {
            toRegister(null)
        }
    }

    private fun toRegister(mobile: String?) {
        Intent(this,RegistrationActivity::class.java).apply {
            putExtra(AppConstants.MOBILE,mobile)
            registerLauncher.launch(this)
        }
    }

    private fun resetView() {
        setTitleText("","")
        binding.idTextRequestOtp.visibility = View.VISIBLE

    }

    @SuppressLint("SetTextI18n")
    private fun setObserver() {

        commonViewModel.isTimerEnable.observe(this) {
            if(it) {
                binding.idEditMobile.isEnabled = false
                binding.idTextRequestOtp.isEnabled = false
                binding.idTextRequestOtp.setTextColor(ResourcesCompat.getColor(resources,R.color.color_secondary,null))
            } else {
                binding.idEditMobile.isEnabled = true
                binding.idTextRequestOtp.apply {
                    isEnabled = true
                    setTextColor(ResourcesCompat.getColor(resources,R.color.color_primary,null))
                    text = "Resend OTP"
                }
            }
        }
    }

    private fun setTitleText(title1: String, title2: String) {
        binding.apply {
            idTextTitle1.text = title1
            idTextTitle2.text = title2
        }
    }


    private fun startCountDownTimer() {

        try {
            countDownTimer = object : CountDownTimer(15000, 1000) {

                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    var seconds = (millisUntilFinished / 1000).toInt()
                    //val minutes = seconds / 60
                    seconds %= 60
                    binding.idTextRequestOtp.text = "Wait ${String.format("%1d",seconds)} seconds"
                    commonViewModel.setTimerEnable(true)
                }

                override fun onFinish() {
                    commonViewModel.setTimerEnable(false)
                }
            }.start()
        }catch (ex : Exception){ ex.printStackTrace() }
    }

}