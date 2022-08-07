package com.codexdroid.sa_assingment.controller.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codexdroid.sa_assingment.utils.STATUS

class CommonViewModels : ViewModel() {

    private var _status = MutableLiveData<STATUS>()
    val status: LiveData<STATUS> = _status

    private var _isTimerEnable = MutableLiveData<Boolean>()
    val isTimerEnable : LiveData<Boolean> = _isTimerEnable


    fun setTimerEnable(isTimerEnable : Boolean) { _isTimerEnable.value = isTimerEnable }
}