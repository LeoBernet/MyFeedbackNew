package com.android.leobernet.myfeedback.act;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.android.leobernet.myfeedback.R;
import com.android.leobernet.myfeedback.info.PrivatePolicy;
import com.android.leobernet.myfeedback.info.SavePrivateData;

public class InfoAct extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        init();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void init(){
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);
    }
    public void onClickPrivatePolicy(View view){
        Intent intent = new Intent(InfoAct.this, PrivatePolicy.class);
        startActivity(intent);
    }

    public void onClickSavePrivateData(View view){
        Intent intent = new Intent(InfoAct.this, SavePrivateData.class);
        startActivity(intent);
    }
}