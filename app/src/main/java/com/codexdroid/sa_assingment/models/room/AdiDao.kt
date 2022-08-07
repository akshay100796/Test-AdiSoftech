package com.codexdroid.sa_assingment.models.room

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AdiDao {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveMobileDetails(mobileData: TableMobile)

    @Transaction
    @Query(value = "SELECT * FROM table_mobiles")
    fun loadMobileDetails() : Flow<List<TableMobile>>

    @Transaction
    @Query(value = "UPDATE table_mobiles SET is_selected=:isSelected WHERE mobile_id=:mobileId")
    fun updateIsSelected (isSelected : Int,mobileId: Int)

    @Transaction
    @Query(value = "UPDATE table_mobiles SET is_order_placed=:isOrderPlaced WHERE mobile_id=:mobileId")
    fun updateIsOrderPlaced (isOrderPlaced : Int,mobileId: Int)



}