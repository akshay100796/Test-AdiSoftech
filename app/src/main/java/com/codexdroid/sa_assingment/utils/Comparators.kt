package com.codexdroid.sa_assingment.utils

import com.codexdroid.sa_assingment.models.locals.LatLong
import com.codexdroid.sa_assingment.models.locals.ShopDetails
import com.codexdroid.sa_assingment.models.room.TableMobile

class DistanceComparator(var latLong: LatLong) : Comparator<ShopDetails> {

    override fun compare(list1: ShopDetails?, list2: ShopDetails?): Int {
        val distance1 = getDistanceInKm(latLong.latitude!!,latLong.longitude!!,list1?.latitude!!,list1.longitude).toDouble()
        val distance2 = getDistanceInKm(latLong.latitude!!,latLong.longitude!!,list1.latitude,list1.longitude).toDouble()
        return distance1.compareTo(distance2)
    }
}

class SelectedDeviceComparator(var latLong: LatLong) : Comparator<TableMobile> {

    override fun compare(table1: TableMobile?, table2: TableMobile?): Int {
        val distance1 = getDistanceInKm(latLong.latitude!!,latLong.longitude!!,table1?.shopLatitude?.toDouble()!!,table2?.shopLongitude?.toDouble()!!).toDouble()
        val distance2 = getDistanceInKm(latLong.latitude!!,latLong.longitude!!,table1.shopLatitude?.toDouble()!!,table2.shopLongitude?.toDouble()!!).toDouble()
        return distance1.compareTo(distance2)
    }
}
