package com.android.leobernet.myfeedback.filter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.leobernet.myfeedback.R;
import com.android.leobernet.myfeedback.databinding.ActivityFilterBinding;
import com.android.leobernet.myfeedback.utils.CountryManager;
import com.android.leobernet.myfeedback.utils.DialogHelper;
import com.android.leobernet.myfeedback.utils.MyConstants;

public class FilterAct extends AppCompatActivity {
    private ActivityFilterBinding rootElement;
    private SharedPreferences pref;
    public static final int COUNTRY_P = 0;
    public static final int CITY_P = 1;
    public static final int STREET_P = 2;
    public static final int HOUSE_P = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootElement = ActivityFilterBinding.inflate(getLayoutInflater());
        setContentView(rootElement.getRoot());
        init();
        fillFilter();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //Слушатель на кнопку
    public void onClickAddFilter(View view) {
        String filter = pref.getString(MyConstants.TEXT_FILTER, "empty");
        if (!filter.equals("empty")) {
            FilterManager.clearFilter(pref);
            rootElement.bAddFilter.setText(R.string.use_filter);
        } else {
            checkEmptyField();
            finish();
        }
    }

    private void init() {
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);
        pref = getSharedPreferences(MyConstants.MAIN_PREF, MODE_PRIVATE);
        String filter = pref.getString(MyConstants.TEXT_FILTER, "empty");
        if (!filter.equals("empty")) rootElement.bAddFilter.setText(R.string.no_use_filter);

    }

    private void fillFilter(){
        String filter = pref.getString(MyConstants.TEXT_FILTER, "empty");
        if (filter.equals("empty"))return;
        String[] arrayFilter = filter.split("\\|");
        if (!arrayFilter[COUNTRY_P].equals("empty")) rootElement.tvCountry.setText(arrayFilter[COUNTRY_P]);
        if (!arrayFilter[CITY_P].equals("empty")) rootElement.tvCity.setText(arrayFilter[CITY_P]);
        if (!arrayFilter[STREET_P].equals("empty")) rootElement.tvStreetAddress.setText(arrayFilter[STREET_P]);
        if (!arrayFilter[HOUSE_P].equals("empty")) rootElement.tvHouseAddress.setText(arrayFilter[HOUSE_P]);
    }

    public void onClickCountry(View view) {
        String city = rootElement.tvCity.getText().toString();
        if (!city.equals(getString(R.string.select_city_f_title))) {
            rootElement.tvCity.setText(R.string.select_city_f_title);
        }
        DialogHelper.INSTANCE.showDialog(this, CountryManager.INSTANCE.getAllCountries(this), (TextView) view);
    }

    public void onClickCity(View view) {
        String country = rootElement.tvCountry.getText().toString();
        if (!country.equals(getString(R.string.select_country_f_title))) {

            DialogHelper.INSTANCE.showDialog(this, CountryManager.INSTANCE.getAllCities(this, country), (TextView) view);

        } else {
            Toast.makeText(this, R.string.first_country, Toast.LENGTH_SHORT).show();
        }
    }

    private void checkEmptyField() {
        String country = rootElement.tvCountry.getText().toString();
        String street = rootElement.edStreetAddress.getText().toString();
        String house = rootElement.edHouseAddress.getText().toString();

        // Посмотреть как в MainActivity else if прописывается и попробовать так же
        if (!street.equals("") && country.equals(getString(R.string.select_country_f_title))) {

            Toast.makeText(this, "Страна не выбрана", Toast.LENGTH_LONG).show();

        } else {
            String textFilter = FilterManager.createTextFilter(rootElement,this);
            String orderBy = FilterManager.createOrderByFilter(textFilter);
            String filter = FilterManager.createFilter(textFilter);
            FilterManager.saveFilter(orderBy,pref,MyConstants.ORDER_BY__FILTER);
            FilterManager.saveFilter(textFilter,pref,MyConstants.TEXT_FILTER);
            FilterManager.saveFilter(filter,pref,MyConstants.FILTER);
            rootElement.bAddFilter.setText(R.string.no_use_filter);

        }
        //Toast.makeText(this, "Не верный фильтр", Toast.LENGTH_LONG).show();
    }


}