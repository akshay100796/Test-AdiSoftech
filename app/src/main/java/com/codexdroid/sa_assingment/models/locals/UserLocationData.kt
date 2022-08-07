package com.codexdroid.sa_assingment.models.locals

data class UserLocationData(
    var country: String,
    var state: String,
    var city: String,
    var town: String,
    var area: String,
    var pin: Int,
    var completeAddress: String?,
    var countryCode: String,
    var latitude: Double,
    var longitude: Double
)


data class UserRegisterData(
    var mobile: String,
    var name: String,
    var date : String,
    var address : String,
    var city : String,
    var latLong : String,
    var profession : String,
    var income : String,
    var email : String,
    var timeMilliSec: Long ) {
    constructor() : this("","","","","","","","","",0L)
}