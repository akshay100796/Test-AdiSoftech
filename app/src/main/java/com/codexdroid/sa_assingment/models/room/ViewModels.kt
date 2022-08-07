package com.codexdroid.sa_assingment.models.room

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.codexdroid.sa_assingment.utils.toJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


/**
private lateinit var viewModel: ViewModels
viewModel = ViewModelProvider(this, ViewModelFactory(requireActivity().application))[ViewModels::class.java]
 * */

class ViewModels(application: Application) : ViewModel() {

    private val dao: AdiDao = AdiDatabase.getRoomInstance(application).dao()
    private val repository : Repository = Repository(dao)

    fun saveMobileData(mobileData : TableMobile) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveMobileData(mobileData)
        }
    }

    fun loadMobileData () : Flow<List<TableMobile>> = repository.loadMobileDetails()

    fun updateIsSelected (isSelected : Int,mobileId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateIsSelected(isSelected, mobileId)
        }
    }

    fun updateIsOrderPlaced (isOrderPlaced : Int,mobileId: Int) {
        viewModelScope.launch (Dispatchers.IO){
            repository.updateIsOrderPlaced(isOrderPlaced, mobileId)
        }

    }



}









class ViewModelFactory (var application: Application) : ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ViewModels::class.java))
            return ViewModels(application = application) as T
        throw IllegalArgumentException("Unknown Class")
    }
}