package com.android.leobernet.myfeedback.screens;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.leobernet.myfeedback.R;
import com.android.leobernet.myfeedback.utils.ImagesManager;
import com.android.leobernet.myfeedback.utils.MyConstants;
import com.android.leobernet.myfeedback.utils.OnBitmapLoaded;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChooseImagesActivity extends AppCompatActivity {
    private String[] uris = new String[3];
    private ImageView imMain, im2, im3;
    private ImageView[] imagesViews = new ImageView[3];
    private ImagesManager imagesManager;
    private final int MAX_IMAGE_SIZE = 2000;
    private OnBitmapLoaded onBitmapLoaded;
    private boolean isImagesLoaded = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_images);
        init();
        getMyIntent();

    }

    private void init() {
        imMain = findViewById(R.id.mainImage);
        im2 = findViewById(R.id.image2);
        im3 = findViewById(R.id.image3);
        uris[0] = "empty";
        uris[1] = "empty";
        uris[2] = "empty";
        imagesViews[0] = imMain;
        imagesViews[1] = im2;
        imagesViews[2] = im3;

        onBitmapLoaded();
        imagesManager = new ImagesManager(this, onBitmapLoaded);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            ArrayList<String> returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            if (returnValue == null) return;
            switch (requestCode) {
                case 1:
                    uris[0] = returnValue.get(0);
                    isImagesLoaded = false;
                    imagesManager.resizeMultiLargeImage(Arrays.asList(uris));
                    break;
                case 2:
                    uris[1] = returnValue.get(0);
                    isImagesLoaded = false;
                    imagesManager.resizeMultiLargeImage(Arrays.asList(uris));
                    break;
                case 3:
                    uris[2] = returnValue.get(0);
                    isImagesLoaded = false;
                    imagesManager.resizeMultiLargeImage(Arrays.asList(uris));
                    break;
            }
        }
    }

    private void onBitmapLoaded() {
        onBitmapLoaded = new OnBitmapLoaded() {
            @Override
            public void onBitmapLoaded(final List<Bitmap> bitmap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < bitmap.size(); i++) {
                            if (bitmap.get(i) != null) imagesViews[i].setImageBitmap(bitmap.get(i));
                        }
                        isImagesLoaded = true;
                    }
                });
            }
        };
    }

    public void onClickMainImage(View view) {
        if (!isImagesLoaded) {
            Toast.makeText(this, R.string.image_load, Toast.LENGTH_SHORT).show();
            return;
        }
        getImage(1);
    }

    public void onClickImage2(View view) {
        if (!isImagesLoaded) {
            Toast.makeText(this, R.string.image_load, Toast.LENGTH_SHORT).show();
            return;
        }
        getImage(2);
    }

    public void onClickImage3(View view) {
        if (!isImagesLoaded) {
            Toast.makeText(this, R.string.image_load, Toast.LENGTH_SHORT).show();
            return;
        }
        getImage(3);
    }

    public void onClickBack(View view) {
        Intent i = new Intent();
        i.putExtra("uriMain", uris[0]);
        i.putExtra("uri2", uris[1]);
        i.putExtra("uri3", uris[2]);

        setResult(RESULT_OK, i);
        finish();
    }

    private void getImage(int index) {
       /* Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(intent, index);*/

        Options options = Options.init()
                .setRequestCode(index)
                .setCount(1)
                .setFrontfacing(false)
                .setExcludeVideos(true)
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT);

        Pix.start(ChooseImagesActivity.this, options);
    }

    private void getMyIntent() {
        Intent i = getIntent();
        if (i != null) {


            uris[0] = i.getStringExtra(MyConstants.NEW_POST_INTENT);
            uris[1] = i.getStringExtra(MyConstants.IMAGE_ID_2);
            uris[2] = i.getStringExtra(MyConstants.IMAGE_ID_3);

            isImagesLoaded = false;

            imagesManager.resizeMultiLargeImage(sortImages(uris));
        }
    }

    private List<String> sortImages(String[] uris) {
        List<String> tempList = new ArrayList<>();
        for (int i = 0; i < uris.length; i++) {
            if (uris[i].startsWith("http")) {
                showHttpImages(uris[i], i);
                tempList.add("empty");
            } else {
                tempList.add(uris[i]);
            }
        }

        return tempList;
    }


    private void showHttpImages(String uri, int pos) {

        Picasso.get().load(uri).into(imagesViews[pos]);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void onClickDeleteMainImage(View view) {
        imMain.setImageResource(R.drawable.add_photo_edit_activity);
        uris[0] = "empty";
    }

    public void onClickDeleteImage2(View view) {
        im2.setImageResource(R.drawable.add_photo_edit_activity);
        uris[1] = "empty";
    }

    public void onClickDeleteImage3(View view) {
        im3.setImageResource(R.drawable.add_photo_edit_activity);
        uris[2] = "empty";
    }
}