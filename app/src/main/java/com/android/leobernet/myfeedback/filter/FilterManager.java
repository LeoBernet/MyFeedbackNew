package com.android.leobernet.myfeedback.filter;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.leobernet.myfeedback.R;
import com.android.leobernet.myfeedback.databinding.ActivityFilterBinding;
import com.android.leobernet.myfeedback.utils.MyConstants;

public class FilterManager {

    public static final String[] orderByKeyWorlds = {"country_", "city_", "address_", "house_"};

    public static void saveFilter(String saveData, SharedPreferences pref, String prefName) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(prefName, saveData);
        editor.apply();
    }

    public static void clearFilter(SharedPreferences pref) {
        pref.edit().clear().apply();
    }
    public static String getFilterText(String filter){
        String[] filterArray = filter.split("\\|");
        StringBuilder textFilter = new StringBuilder();
        for (int i = 0; i < filterArray.length; i++){
            if (!filterArray[i].equals("empty") && i != 4){
                if (i !=0)textFilter.append(", ");
                textFilter.append(filterArray[i]);
            }
        }
        return textFilter.toString();
    }

    public static String createTextFilter(ActivityFilterBinding rootElement, Context context) {
        String country = rootElement.tvCountry.getText().toString();
        String city = rootElement.tvCity.getText().toString();
        String address = rootElement.edStreetAddress.getText().toString();
        String house = rootElement.edHouseAddress.getText().toString();

        if (country.equals(context.getString(R.string.select_country_f_title))) country = "empty";
        if (city.equals(context.getString(R.string.select_city_f_title))) city = "empty";
        if (address.equals("")) address = "empty";
        if (house.equals("")) house = "empty";
        return country + "|" + city + "|" + address + "|" + house;
    }

    public static String createOrderByFilter(String filter) {
        String[] filterTempArray = filter.split("\\|");
        StringBuilder orderByFilter = new StringBuilder();
        for (int i =0; i < filterTempArray.length; i++){
            if (!filterTempArray[i].equals("empty")){
                orderByFilter.append(orderByKeyWorlds[i]);
            }
        }
        orderByFilter.append("time");
        return orderByFilter.toString();
    }

    public static String createFilter(String filter) {
        String[] filterTempArray = filter.split("\\|");
        StringBuilder orderByFilter = new StringBuilder();
        for (String s : filterTempArray) {

            if (!s.equals("empty")) {
                orderByFilter.append(s);
                orderByFilter.append("_");
            }
        }
        return orderByFilter.toString();
    }


}
