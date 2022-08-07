package com.codexdroid.sa_assingment.models.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.codexdroid.sa_assingment.utils.AppConstants

@Database(entities = [TableMobile::class],version = 1,exportSchema = false)
abstract class AdiDatabase : RoomDatabase() {

    abstract fun dao() : AdiDao

    companion object {
        private var INSTANCE : AdiDatabase? = null

        fun getRoomInstance(context: Context): AdiDatabase{
            synchronized(this){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context,AdiDatabase::class.java,
                        AppConstants.DATABASE_NAME).build()
                    return INSTANCE as AdiDatabase
                }
                return INSTANCE as AdiDatabase
            }
        }
    }
}