package com.htc.whether.utils

import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MyDataManager(context: Context) {

    val sharedPreference =  context.getSharedPreferences("location_preferences",Context.MODE_PRIVATE)

     fun saveLatLong(latitude: String, longitude: String) {
         var editor = sharedPreference.edit()
         editor.putString("LATITUDE",latitude)
         editor.putString("LONGITUDE",longitude)
         editor.commit()
    }

    fun getLatLong(): String{
       return sharedPreference.getString("LATITUDE","0.0") + "," + sharedPreference.getString("LONGITUDE","0.0")
    }

}
