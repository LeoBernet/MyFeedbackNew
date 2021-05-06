package com.android.leobernet.myfeedback;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.leobernet.myfeedback.adapter.ImageAdapter;
import com.android.leobernet.myfeedback.db.NewPost;
import com.android.leobernet.myfeedback.utils.MyConstants;

import java.util.ArrayList;
import java.util.List;

public class ShowLayoutActivity extends AppCompatActivity {

    private TextView tvTitle,tvAddress,tvDisc,tvHouse;
    private List<String> imagesUris;
    private ImageAdapter imAdapter;
    private TextView tvImagesCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_layout);
        init();
    }
    private void init() {
        imagesUris = new ArrayList<>();
        ViewPager vp = findViewById(R.id.view_pager);
        imAdapter = new ImageAdapter(this);
        vp.setAdapter(imAdapter);
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                String dataText = position + 1 + "/" + imagesUris.size();
                tvImagesCounter.setText(dataText);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tvImagesCounter = findViewById(R.id.tvImagesCounter);

        tvTitle = findViewById(R.id.tvTitle1);
        tvAddress = findViewById(R.id.tvAddress);
        tvDisc = findViewById(R.id.tvDisc1);
        tvHouse = findViewById(R.id.tvHouseAddress);

        if (getIntent() != null) {
            Intent i =getIntent();

            assert i != null;
            NewPost newPost = (NewPost)i.getSerializableExtra(MyConstants.NEW_POST_INTENT);

            if (newPost == null) return;
            tvTitle.setText(newPost.getTitle());
            tvAddress.setText(newPost.getAddress());
            tvDisc.setText(newPost.getDisc());
            tvHouse.setText(newPost.getHouse());

            String[] images = new String[3];
            images[0] = newPost.getImageId();
            images[1] = newPost.getIm_id2();
            images[2] = newPost.getIm_id3();

            for (String s : images)
            {
                if (!s.equals("empty"))imagesUris.add(s);
            }

            imAdapter.updateImages(imagesUris);
            String dataText;
            if (imagesUris.size() > 0) {
                dataText = 1 + "/" + imagesUris.size();
            }
            else
            {
                dataText = 0 + "/" + imagesUris.size();
            }
            tvImagesCounter.setText(dataText);
           // Picasso.get().load(i.getStringExtra(MyConstants.IMAGE_ID)).into(imMain);
        }
    }
}