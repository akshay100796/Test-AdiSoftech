package com.codexdroid.sa_assingment.ui.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.codexdroid.sa_assingment.utils.toJson
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class LoginActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefManager: PrefManager
    private lateinit var countDownTimer: CountDownTimer
    private var serverOtp : String = "123456"
    private lateinit var auth: FirebaseAuth

    private lateinit var clientOtp: String
    var userData: UserRegisterData? = null
    private var userInputMobile : String? = null


    private val commonViewModel : CommonViewModels by viewModels()

    private var registerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        setTitleText("","")
    }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            serverOtp = credential.smsCode.toString()
            Log.d("AXE","Credentials : ${credential.smsCode}")
        }

        override fun onVerificationFailed(e: FirebaseException) {
            e.printStackTrace()

            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    showErrorDialog("Invalid Credentials")
                }
                is FirebaseTooManyRequestsException -> {
                    showErrorDialog("Too Many Request, Pls try later, OR")
                }
                else -> {
                    showErrorDialog(e.message!!)
                }
            }
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            Log.d("AXE","$verificationId ||| $token ===> | ${token.describeContents()}")
            val credential = PhoneAuthProvider.getCredential(verificationId,serverOtp)
            serverOtp = credential.smsCode.toString()
            Toast.makeText(this@LoginActivity,"Default OTP : 123456",Toast.LENGTH_LONG).show()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this@LoginActivity)
            .setTitle("Testing Credentials")
            .setMessage("$message \n\nMobile : 7757888063, OTP : 123456\n\nDo You Want to continue Default no or Your Number?")
            .setPositiveButton("Default No"){d,_ ->

                binding.idEditMobile.setText("7757888063")
                binding.idInputOtp.setText("123456")
                userInputMobile = binding.idEditMobile.text.toString()

                databaseReference.addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        label@for (snap in snapshot.children) {

                            if(snap.key == userInputMobile) {
                                userData = snap.getValue(UserRegisterData::class.java)
                                break@label
                            }
                        }

                        Log.d("AXE","Own No Data : ${toJson(userData)}")
                        saveDataAndLogin()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }
            .setNegativeButton("My Own No") { d,_ ->

                val mobile = binding.idEditMobile.text.toString()
                binding.idEditMobile.setText(mobile)
                val random = Random.nextInt(111111,999999)
                binding.idInputOtp.setText(random.toString())
                saveDataAndLogin()

                d.dismiss()
            }
            .show()
    }

    private fun saveDataAndLogin() {
        prefManager.saveToken("CQGLLeDakgpZ5qwDSMEfpZl+SSELcxJcX9u78YLgJr4=") //Encryption Text Of "Hello There"
        prefManager.saveUserRegisterData(userData)
        startActivity(Intent(this,HomeActivity::class.java))
        finish()
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
        //auth = Firebase.auth
        auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(false)
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

        binding.idTextRequestOtp.setOnClickListener {

            setTitleText("Checking","Mobile Number")
            userInputMobile = binding.idEditMobile.text.toString()

            databaseReference.addValueEventListener(object : ValueEventListener{
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    label@for (snap in snapshot.children) {

                        if(snap.key == userInputMobile) {
                            userData = snap.getValue(UserRegisterData::class.java)
                            break@label
                        }
                    }

                    Log.d("AXE","User Data OTP : ${toJson(userData)}")
                    if (userData != null) {
                        //Mobile Found, visible element and take otp from server
                        setTitleText("","")
                        startCountDownTimer()
                        binding.idButtonLogin.visibility = View.VISIBLE
                        binding.idInputOtp.visibility = View.VISIBLE
                        binding.idInputOtp.isEnabled = true

                        if (userInputMobile == "7757888063") { //pre-define
                            binding.idInputOtp.setText("123456")
                            Toast.makeText(this@LoginActivity,"Test Number OTP : 123456",Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@LoginActivity,"OTP Sent",Toast.LENGTH_LONG).show()
                            val options = PhoneAuthOptions.newBuilder(auth)
                                .setPhoneNumber("+91${userInputMobile}")
                                .setTimeout(30L, TimeUnit.SECONDS)
                                .setActivity(this@LoginActivity)
                                .setCallbacks(callbacks)
                                .build()
                            PhoneAuthProvider.verifyPhoneNumber(options)
                        }

                    } else {
                        //Mobile No Not Found navigate to register screen
                        toRegister(userInputMobile)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        binding.idInputOtp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                binding.idButtonLogin.isEnabled = charSequence?.length == 6
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.idButtonLogin.setOnClickListener {
            clientOtp = binding.idInputOtp.text.toString()

            //Considering User will enter valid otp so without validating, continuing to home screen
           saveDataAndLogin()
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