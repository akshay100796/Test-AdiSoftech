package com.codexdroid.sa_assingment.models.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "table_mobiles")
class TableMobile (

    @PrimaryKey
    @ColumnInfo(name = "mobile_id")
    var mobileId : String,

    @ColumnInfo(name = "mobile_name")
    var mobileName : String?,

    @ColumnInfo(name = "mobile_image")
    var mobileImage : String?,

    @ColumnInfo(name = "mobile_price")
    var mobilePrice : String?,

    @ColumnInfo(name = "mobile_order_status")
    var mobileOrderStatus: String?,

    @ColumnInfo(name = "shop_id")
    var shopId : String?,

    @ColumnInfo(name = "shop_name")
    var shopName: String?,

    @ColumnInfo(name = "shop_image_url")
    var shopImageUrl : String?,

    @ColumnInfo(name = "shop_image_qr_code")
    var shopImageQR_Code : String?,

    @ColumnInfo(name = "shop_latitude")
    var shopLatitude : String?,

    @ColumnInfo(name = "shop_longitude")
    var shopLongitude : String?,

    @ColumnInfo(name = "shop_last_visit")
    var lastVisit : String?,

    @ColumnInfo(name = "current_time_milli_sec")
    var currentTimeMilliSec: String?,

    @ColumnInfo(name = "is_selected")
    var isSelected : Int = 0,

    @ColumnInfo(name = "is_order_placed")
    var isOrderPlaced : Int = 0
)