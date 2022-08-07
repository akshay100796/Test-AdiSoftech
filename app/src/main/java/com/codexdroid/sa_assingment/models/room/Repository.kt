package com.codexdroid.sa_assingment.models.room

import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow


class Repository(private var dao: AdiDao) {

    fun saveMobileData(mobile: TableMobile){
        dao.saveMobileDetails(mobile)
    }

    fun loadMobileDetails() : Flow<List<TableMobile>> = dao.loadMobileDetails()

    fun updateIsSelected (isSelected : Int,mobileId: Int) {
        dao.updateIsSelected(isSelected, mobileId)
    }

    fun updateIsOrderPlaced (isOrderPlaced : Int,mobileId: Int) {
        dao.updateIsOrderPlaced(isOrderPlaced, mobileId)
    }

}