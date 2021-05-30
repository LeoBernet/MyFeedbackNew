package com.android.leobernet.myfeedback.status;

import com.android.leobernet.myfeedback.db.NewPost;

public class StatusManager {

    public static StatusItem fillStatusItem(NewPost post){
        StatusItem stItem = new StatusItem();
        String time = post.getTime();
        String cat = post.getCat();
        String country = post.getCountry();
        String city = post.getCity();
        String address = post.getAddress().toLowerCase();
        String house = post.getHouse().toLowerCase();
        String name = post.getName().toLowerCase();

        stItem.name_time = name + "_" + time;
        stItem.cat_address_time = cat + "_" + address + "_" + time;
        stItem.address_time = address + "_" + time;

        stItem.country_address_time = country + "_" + address + "_" + time;
        stItem.country_city_address_time = country + "_" + city + "_" + address + "_" + time;
        stItem.country_city_address_house_time = country + "_" +city + "_" + address + "_" + house + "_" + time;

        stItem.country_name_time = country + "_" + name + "_" + time;
        stItem.country_city_name_time = country + "_" + city + "_" + name + "_" + time;

        stItem.country_city_time = country + "_" + city + "_" + time;
        stItem.country_time = country + "_" + time;



        stItem.cat_country_address_time = cat + "_" + country + "_" + address + "_" + time;
        stItem.cat_country_city_address_time = cat + "_" + country + "_" + city + "_" + address + "_" + time;
        stItem.cat_country_city_address_house_time = cat + "_" + country + "_" +city + "_" + address + "_" + house + "_" + time;

        stItem.cat_country_name_time = cat + "_" + country + "_" + name + "_" + time;
        stItem.cat_country_city_name_time = cat + "_" + country + "_" + city + "_" + name + "_" + time;

        stItem.cat_country_city_time = cat + "_" + country + "_" + city + "_" + time;
        stItem.cat_country_time = cat + "_" + country + "_" + time;

        return stItem;
    }
}
