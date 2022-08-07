package com.codexdroid.sa_assingment.utils

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.GsonBuilder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


enum class STATUS {
    LOADING, DONE, ERROR, SOCKET_TIMEOUT, NOT_FOUND,ONLINE, OFFLINE, DATA_NOT_FOUND, EMPTY, UNAUTHORIZED
}

fun toJson(data: Any?): String? = GsonBuilder().serializeNulls().create().toJson(data)

fun isConnectedToInternet(context: Context) : Boolean = NetworkUtils.isConnectedToMobileInternet(context) || NetworkUtils.isConnectedToWifi(context)


@SuppressLint("SimpleDateFormat")
fun getDateOnlyForFilter(time: Long?): String{
    val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
    return simpleDateFormat.format(time)
}


fun getDistanceInKm(lat1:Double, long1: Double, lat2:Double, long2:Double) : String{

    val piRadian = PI.div(180.0)

    val phi1 = lat1 * piRadian
    val phi2 = lat2 * piRadian
    val lam1 = long1 * piRadian
    val lam2 = long2 * piRadian

    val distance = 6371.01 * kotlin.math.acos(sin(phi1) * sin(phi2) + cos(phi1) * cos(phi2) * cos(lam2 - lam1))
    return String.format("%.2f",distance)
}

fun getTimeDurationFromMilliSec(time: Long?): String = durationTillNow(SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa").format(time))

@SuppressLint("SimpleDateFormat")
fun durationTillNow(userRegistrationDate : String): String{

    val outputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa")
    val registeredDate : Date = outputFormat.parse(userRegistrationDate)!!
    val todayDate : Date = outputFormat.parse(SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa").format(
        Calendar.getInstance().time))!!

    val difference = todayDate.time - registeredDate.time
    val diffInMin =  TimeUnit.MILLISECONDS.toMinutes(difference) % 60
    val diffInHour= TimeUnit.MILLISECONDS.toHours(difference) % 24

    val diffInDays= TimeUnit.MILLISECONDS.toDays(difference) % 365
    val diffInYear: Long =  TimeUnit.MILLISECONDS.toDays(difference)/365L

    val diffInMonth = if(diffInDays > 30){ diffInDays/ 30 } else 0

    return when {
        diffInYear > 0 -> { "$diffInYear Year(s) Ago" }
        diffInMonth > 0 -> { "$diffInMonth Month(s) Ago "}
        diffInDays > 0 -> { "$diffInDays day(s) Ago" }
        diffInHour > 0 -> { "$diffInHour Hr Ago" }
        diffInMin > 0 -> { "$diffInMin Min(s) Ago"}
        else -> { "few sec(s) Ago" }
    }
}