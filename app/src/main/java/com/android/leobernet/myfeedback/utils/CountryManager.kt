package com.android.leobernet.myfeedback.utils

import android.content.Context
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList

object CountryManager {
    fun getAllCountries(context: Context): ArrayList<String> {
        val tempArray = ArrayList<String>()
        try {
            val inputStream: InputStream = context.assets.open("countriesToCities.json")
            val size: Int = inputStream.available()
            val byteArray = ByteArray(size)
            inputStream.read(byteArray)
            val jFile = String(byteArray)
            val jObject = JSONObject(jFile)
            val countriesNames = jObject.names()
            if (countriesNames != null) {
                for (n in 0 until countriesNames.length()) {
                    tempArray.add(countriesNames.getString(n))
                }
            }

        } catch (e: IOException) {

        }
        return tempArray
    }

    fun getAllCities(context: Context, country : String ): ArrayList<String> {
        val tempArray = ArrayList<String>()
        try {
            val inputStream: InputStream = context.assets.open("countriesToCities.json")
            val size: Int = inputStream.available()
            val byteArray = ByteArray(size)
            inputStream.read(byteArray)
            val jFile = String(byteArray)
            val jObject = JSONObject(jFile)
            val citys = jObject.getJSONArray(country)

                for (n in 0 until citys.length()) {
                    tempArray.add(citys.getString(n))
                }

        } catch (e: IOException) {

        }
        return tempArray
    }

    fun filterListData(list : ArrayList<String>,searchText : String?) : ArrayList<String>{
        val tempList = ArrayList<String>()
        if (searchText == null){
            tempList.add("No Result")
            return tempList
        }
        for (selection : String in list){
            if (selection.toLowerCase(Locale.ROOT).startsWith(searchText.toLowerCase(Locale.ROOT)))
                tempList.add(selection)
        }
        if(tempList.isEmpty()) tempList.add("No result")
        return tempList
    }
}