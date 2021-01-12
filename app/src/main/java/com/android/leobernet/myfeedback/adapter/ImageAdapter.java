package com.android.leobernet.myfeedback.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.android.leobernet.myfeedback.R;
import com.android.leobernet.myfeedback.utils.ImagesManager;
import com.android.leobernet.myfeedback.utils.OnBitmapLoaded;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends PagerAdapter implements OnBitmapLoaded {
    private Activity context;
    private LayoutInflater inflater;
    private List<Bitmap> bmList;
    private ImagesManager imagesManager;

    public ImageAdapter(Activity context) {
        this.context = context;
        imagesManager = new ImagesManager(context,this);
        inflater = LayoutInflater.from(context);
        bmList = new ArrayList<>();
    }

    @Override
    public int getCount() {

        return bmList.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = inflater.inflate(R.layout.pager_item, container, false);
        ImageView imItem = view.findViewById(R.id.imageViewPager);

            imItem.setImageBitmap(bmList.get(position));


        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        container.removeView((LinearLayout) object);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    public void updateImages(List<String> images) {

            imagesManager.resizeMultiLargeImage(images);
    }

    @Override
    public void onBitmapLoaded(final List<Bitmap> bitmap) {

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bmList.clear();
                bmList.addAll(bitmap);
                notifyDataSetChanged();
            }
        });
    }
}
