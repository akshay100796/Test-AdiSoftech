package com.codexdroid.sa_assingment.utils

import android.content.Context
import android.content.SharedPreferences
import com.codexdroid.sa_assingment.models.locals.UserLocationData
import com.codexdroid.sa_assingment.models.locals.UserRegisterData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class PrefManager (private val context: Context) {

    @Volatile
    var sharePref: SharedPreferences? = null
    @Volatile
    var prefManager: PrefManager? = null
    @Volatile
    var gson: Gson? = null

    init {
        initPrefIfNull()
    }

    private fun getGsonInstance(): Gson? {
        if (gson == null) gson = Gson()
        return gson
    }

    private fun initPrefIfNull() {
        synchronized(context) {
            if (sharePref == null) {
                sharePref =
                    context.getSharedPreferences(AppConstants.SHARE_PREF_NAME, Context.MODE_PRIVATE)
                sharePref?.edit()?.apply()
            }
        }
    }

    fun getPreference(): PrefManager? {
        if (prefManager == null) prefManager = PrefManager(context)
        return prefManager
    }

    //Integer Data
    fun saveIntData(key: String, data: Int) {
        sharePref?.edit()?.putInt(key, data)?.apply()
    }

    fun getIntData(key: String): Int? = sharePref?.getInt(key, 0)

    //Long Data
    fun saveLongData(key: String, data: Long) {
        sharePref?.edit()?.putLong(key, data)?.apply()
    }

    fun getLongData(key: String): Long? = sharePref?.getLong(key, 0L)

    //Float Data
    fun saveFloatData(key: String, data: Float) {
        sharePref?.edit()?.putFloat(key, data)?.apply()
    }

    fun getFloatData(key: String): Float? = sharePref?.getFloat(key, 0.0F)

    //Boolean Data
    fun saveBooleanData(key: String, data: Boolean) {
        sharePref?.edit()?.putBoolean(key, data)?.apply()
    }

    fun getBooleanData(key: String): Boolean? = sharePref?.getBoolean(key, false)

    //String data
    fun saveStringData(key: String, data: String) {
        sharePref?.edit()?.putString(key, data)?.apply()
    }

    fun getStringData(key: String): String? = sharePref?.getString(key, null)


    //Token
    fun saveToken(token: String) {
        sharePref?.edit()?.putString(AppConstants.TOKEN, token)?.apply()
    }

    fun getToken(): String? = sharePref?.getString(AppConstants.TOKEN, null)

    fun clearAll(){ sharePref?.edit()?.clear()?.apply() }

    //**********************************************************************************************

    fun saveUserLocationData(data : UserLocationData?){
        if(sharePref == null) initPrefIfNull()
        val editor = sharePref?.edit()
        val gson = getGsonInstance()
        editor?.putString(AppConstants.LOCATION_DATA,gson?.toJson(data))
        editor?.apply()
    }

    fun loadUserLocationData(): UserLocationData?{
        if(sharePref == null) initPrefIfNull()
        val editor = sharePref?.edit()
        val gson = getGsonInstance()
        val json = sharePref?.getString(AppConstants.LOCATION_DATA,null)
        val type = object : TypeToken<UserLocationData>(){}.type
        val locationData = gson?.fromJson<UserLocationData>(json,type)
        editor?.apply()
        return locationData
    }


    fun saveUserRegisterData(data : UserRegisterData?){
        if(sharePref == null) initPrefIfNull()
        val editor = sharePref?.edit()
        val gson = getGsonInstance()
        editor?.putString(AppConstants.USER_REGISTER_DATA,gson?.toJson(data))
        editor?.apply()
    }

    fun loadUserRegisterData(): UserRegisterData?{
        if(sharePref == null) initPrefIfNull()
        val editor = sharePref?.edit()
        val gson = getGsonInstance()
        val json = sharePref?.getString(AppConstants.USER_REGISTER_DATA,null)
        val type = object : TypeToken<UserRegisterData>(){}.type
        val locationData = gson?.fromJson<UserRegisterData>(json,type)
        editor?.apply()
        return locationData
    }


}