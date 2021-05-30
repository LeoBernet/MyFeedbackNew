package com.android.leobernet.myfeedback.act;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.leobernet.myfeedback.R;
import com.android.leobernet.myfeedback.adapter.ImageAdapter;
import com.android.leobernet.myfeedback.databinding.EditLayoutBinding;
import com.android.leobernet.myfeedback.db.DbManager;
import com.android.leobernet.myfeedback.db.NewPost;
import com.android.leobernet.myfeedback.info.PrivatePolicy;
import com.android.leobernet.myfeedback.screens.ChooseImagesActivity;
import com.android.leobernet.myfeedback.status.StatusManager;
import com.android.leobernet.myfeedback.utils.CountryManager;
import com.android.leobernet.myfeedback.utils.DialogHelper;
import com.android.leobernet.myfeedback.utils.ImagesManager;
import com.android.leobernet.myfeedback.utils.MyConstants;
import com.android.leobernet.myfeedback.utils.OnBitmapLoaded;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditActivity extends AppCompatActivity implements OnBitmapLoaded {


    private EditLayoutBinding rootElement;
    private final int MAX_UPLOAD_IMAGE_SIZE = 1920;
    private StorageReference mStorageRef;
    private String[] uploadUri = new String[3];
    private String[] uploadNewUri = new String[3];
    private DatabaseReference dRef;
    private FirebaseAuth mAuth;
    private boolean edit_state = false;
    private String temp_cat = "";
    private String temp_uid = "";
    private String temp_time = "";
    private String temp_key = "";
    private String temp_total_views = "";
    private boolean is_image_update = false;
    private ProgressDialog pd;
    private int load_image_counter = 0;
    private List<String> imagesUris;
    private ImageAdapter imAdapter;
    private List<Bitmap> bitmapArrayList;
    private ImagesManager imagesManager;
    private boolean isImagesLoaded = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootElement = EditLayoutBinding.inflate(getLayoutInflater());
        setContentView(rootElement.getRoot());
        init();
    }

    private void init() {

        //подчеркивает весь текст
        TextView textView = (TextView) findViewById(R.id.discRules);
        textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        imagesManager = new ImagesManager(this, this);
        imagesUris = new ArrayList<>();
        bitmapArrayList = new ArrayList<>();
        imAdapter = new ImageAdapter(this);
        rootElement.viewPager.setAdapter(imAdapter);
        rootElement.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                String dataText = position + 1 + "/" + imagesUris.size();
                rootElement.tvImagesCounter.setText(dataText);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        uploadUri[0] = "empty";
        uploadUri[1] = "empty";
        uploadUri[2] = "empty";

        pd = new ProgressDialog(this);

        // если что удалить эту часть this.getResources().getString
        pd.setMessage(this.getResources().getString(R.string.loading));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource
                (this, R.array.category_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rootElement.spinner.setAdapter(adapter);
        mStorageRef = FirebaseStorage.getInstance().getReference("Images");
        getMyIntent();
    }

    private void getMyIntent() {
        if (getIntent() != null) {
            Intent i = getIntent();
            edit_state = i.getBooleanExtra(MyConstants.EDIT_STATE, false);
            if (edit_state) setDataAds(i);
        }
    }

    private void setDataAds(Intent i) {
        // Picasso.get().load(i.getStringExtra(MyConstants.IMAGE_ID)).into(imItem);
        NewPost newPost = (NewPost) i.getSerializableExtra(MyConstants.NEW_POST_INTENT);
        if (newPost == null) return;

        rootElement.edTittle.setText(newPost.getTitle());
        rootElement.edName.setText(newPost.getName());
        rootElement.edAddress.setText(newPost.getAddress());
        rootElement.edDisc.setText(newPost.getDisc());

        temp_cat = newPost.getCat();
        temp_uid = newPost.getUid();
        temp_time = newPost.getTime();
        temp_key = newPost.getKey();
        temp_total_views = newPost.getTotal_views();

        uploadUri[0] = newPost.getImageId();
        uploadUri[1] = newPost.getIm_id2();
        uploadUri[2] = newPost.getIm_id3();

        for (String s : uploadUri) {
            if (!s.equals("empty")) imagesUris.add(s);
        }
        isImagesLoaded = true;
        imAdapter.updateImages(imagesUris);

        String dataText;
        if (imagesUris.size() > 0) {
            dataText = 1 + "/" + imagesUris.size();
        } else {
            dataText = 0 + "/" + imagesUris.size();
        }
        rootElement.tvImagesCounter.setText(dataText);
    }

    private void uploadImage() {

        if (load_image_counter < uploadUri.length) {

            if (!uploadUri[load_image_counter].equals("empty")) {
                Bitmap bitMap = bitmapArrayList.get(load_image_counter);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                assert bitMap != null;

                bitMap.compress(Bitmap.CompressFormat.JPEG, 40, out);
                byte[] byteArray = out.toByteArray();
                final StorageReference mRef = mStorageRef
                        .child(System.currentTimeMillis() + "_image");
                UploadTask up = mRef.putBytes(byteArray);
                Task<Uri> task = up.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        return mRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.getResult() == null) return;
                        uploadUri[load_image_counter] = task.getResult().toString();
                        load_image_counter++;
                        if (load_image_counter < uploadUri.length) {
                            uploadImage();
                        } else {
                            publishPost();
                            Toast.makeText(EditActivity.this,
                                    R.string.photo_uploaded,
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
            } else {
                load_image_counter++;
                uploadImage();
            }
        } else {
            publishPost();
            finish();
        }
    }

    private void uploadUpdateImage() {
        Bitmap bitMap = null;

        if (load_image_counter < uploadUri.length) {

            //1 - Если ссылка на старой позиции равна ссылке на новой позиции
            if (uploadUri[load_image_counter].equals(uploadNewUri[load_image_counter])) {
                load_image_counter++;
                uploadUpdateImage();
            }
            //2 - Если ссылка на старой позиции не равна ссылке на новой позиции и ссылка на новой позиции не "empty"
            else if (!uploadUri[load_image_counter].equals(uploadNewUri[load_image_counter]) &&
                    !uploadNewUri[load_image_counter].equals("empty")) {

                bitMap = bitmapArrayList.get(load_image_counter);

            }
            //3 - Если в старом массиве не "empty" а в новом на той же позиции "empty" значит
            // удалить старую ссылку и картинку
            else if (!uploadUri[load_image_counter].equals("empty")
                    && uploadNewUri[load_image_counter].equals("empty")) {

                StorageReference mRef = FirebaseStorage.getInstance().getReferenceFromUrl(uploadUri[load_image_counter]);
                mRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        uploadUri[load_image_counter] = "empty";
                        load_image_counter++;
                        if (load_image_counter < uploadUri.length) {
                            uploadUpdateImage();
                        } else {
                            publishPost();
                        }
                    }
                });
            }

            if (bitMap == null) return;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitMap.compress(Bitmap.CompressFormat.JPEG, 40, out);
            byte[] byteArray = out.toByteArray();
            final StorageReference mRef;

            if (!uploadUri[load_image_counter].equals("empty")) {
                //2 - A Если ссылка на старой позиции не равна empty то перезаписываем старую на новую
                mRef = FirebaseStorage.getInstance().getReferenceFromUrl(uploadUri[load_image_counter]);
            } else {
                //2 - B Если ссылка на старой позиции равна empty то записываем новую картинку на firebase storage
                mRef = mStorageRef.child(System.currentTimeMillis() + "_image");
            }

            UploadTask up = mRef.putBytes(byteArray);
            Task<Uri> task = up.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    return mRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    uploadUri[load_image_counter] = task.getResult().toString();

                    load_image_counter++;
                    if (load_image_counter < uploadUri.length) {
                        uploadUpdateImage();
                    } else {
                        publishPost();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        } else {

            publishPost();
        }
    }

    public void onClickSavePost(View view) {
        if (!isFieldEmpty()) {
            Toast.makeText(this, R.string.empty_field_error, Toast.LENGTH_LONG).show();
            return;
        }
        if (isImagesLoaded) {
            pd.show();
            if (!edit_state) {
                uploadImage();
            } else {
                if (is_image_update) {
                    uploadUpdateImage();
                } else {
                    publishPost();
                }
            }
        } else {
            Toast.makeText(this, R.string.image_load, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isFieldEmpty() {

        String country = rootElement.tvSelectCountry.getText().toString();
        String city = rootElement.tvSelectCitty.getText().toString();
        String address = rootElement.edAddress.getText().toString();
        String title = rootElement.edTittle.getText().toString();
        String disc = rootElement.edDisc.getText().toString();
        String house = rootElement.edHouseAddress.getText().toString();
        CheckBox checkBox = rootElement.editCheckBox;


        return (!getString(R.string.select_country).equals(country)
                && !getString(R.string.select_city).equals(city)
                && !TextUtils.isEmpty(address) && !TextUtils.isEmpty(title)
                && !TextUtils.isEmpty(disc) && !TextUtils.isEmpty(house)
                && checkBox.isChecked());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 15 && data != null) {
            if (resultCode == RESULT_OK) {
                is_image_update = true;

                imagesUris.clear();
                String[] tempUriArray = getUrisFromChoose(data);
                isImagesLoaded = false;
                imagesManager.resizeMultiLargeImage(Arrays.asList(tempUriArray));
                for (String s : tempUriArray) {
                    if (!s.equals("empty")) imagesUris.add(s);
                }
                imAdapter.updateImages(imagesUris);
                String dataText;
                if (imagesUris.size() > 0) {
                    dataText = rootElement.viewPager.getCurrentItem() + 1 + "/" + imagesUris.size();
                } else {
                    dataText = 0 + "/" + imagesUris.size();
                }
                rootElement.tvImagesCounter.setText(dataText);
            }
        }
    }

    private String[] getUrisFromChoose(Intent data) {

        if (edit_state) {

            uploadNewUri[0] = data.getStringExtra("uriMain");
            uploadNewUri[1] = data.getStringExtra("uri2");
            uploadNewUri[2] = data.getStringExtra("uri3");
            return uploadNewUri;
        } else {

            uploadUri[0] = data.getStringExtra("uriMain");
            uploadUri[1] = data.getStringExtra("uri2");
            uploadUri[2] = data.getStringExtra("uri3");
            return uploadUri;
        }
    }

    public void onClickImage(View view) {
        Intent i = new Intent(EditActivity.this, ChooseImagesActivity.class);
        i.putExtra(MyConstants.NEW_POST_INTENT, uploadUri[0]);
        i.putExtra(MyConstants.IMAGE_ID_2, uploadUri[1]);
        i.putExtra(MyConstants.IMAGE_ID_3, uploadUri[2]);
        startActivityForResult(i, 15);
    }

    private void publishPost() {
        mAuth = FirebaseAuth.getInstance();
        dRef = FirebaseDatabase.getInstance().getReference(DbManager.MAIN_ADS_PATH);

        NewPost post = new NewPost();
        post.setImageId(uploadUri[0]);
        post.setIm_id2(uploadUri[1]);
        post.setIm_id3(uploadUri[2]);

        post.setCountry(rootElement.tvSelectCountry.getText().toString());
        post.setCity(rootElement.tvSelectCitty.getText().toString());
        post.setAddress(rootElement.edAddress.getText().toString());
        post.setTitle(rootElement.edTittle.getText().toString());
        post.setName(rootElement.edName.getText().toString());
        post.setDisc(rootElement.edDisc.getText().toString());
        post.setHouse(rootElement.edHouseAddress.getText().toString());

        if (edit_state) {
            updatePost(post);
        } else {
            savePost(post);
        }
    }

    private void updatePost(NewPost post) {

        if (mAuth.getUid() == null) return;
        post.setKey(temp_key);
        post.setCat(temp_cat);
        post.setTime(temp_time);
        post.setUid(temp_uid);
        post.setTotal_views(temp_total_views);
        dRef.child(temp_key).child("status").setValue(StatusManager.fillStatusItem(post));
        dRef.child(temp_key).child(mAuth.getUid()).child("feedback").setValue(post)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(EditActivity.this,
                                R.string.feedback_loaded,
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void savePost(NewPost post) {

        if (mAuth.getUid() == null) return;
        String key = dRef.push().getKey();
        post.setKey(key);
        post.setCat(rootElement.spinner.getSelectedItem().toString());
        post.setTime(String.valueOf(System.currentTimeMillis()));
        post.setUid(mAuth.getUid());
        post.setTotal_views("0");

        if (key != null) {

            dRef.child(key).child(mAuth.getUid()).child("feedback").setValue(post);
            dRef.child(key).child("status").setValue(StatusManager.fillStatusItem(post));
        }
        Intent i = new Intent();
        i.putExtra("cat", rootElement.spinner.getSelectedItem().toString());
        setResult(RESULT_OK, i);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pd.dismiss();
    }

    @Override
    public void onBitmapLoaded(final List<Bitmap> bitmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bitmapArrayList.clear();
                bitmapArrayList.addAll(bitmap);
                isImagesLoaded = true;
            }
        });
    }

    public void onClickPrivatePolicy(View view) {
        Intent intent = new Intent(EditActivity.this, PrivatePolicy.class);
        startActivity(intent);
    }

    public void onClickSetCountry(View view) {
        String city = rootElement.tvSelectCitty.getText().toString();
        if (!city.equals(getString(R.string.select_city))) {
            rootElement.tvSelectCitty.setText(R.string.select_city);
        }
        DialogHelper.INSTANCE.showDialog(this, CountryManager.INSTANCE.getAllCountries(this), (TextView) view);
    }

    public void onClickSetCity(View view) {
        String country = rootElement.tvSelectCountry.getText().toString();
        if (!country.equals(getString(R.string.select_country))) {

            DialogHelper.INSTANCE.showDialog(this, CountryManager.INSTANCE.getAllCities(this, country), (TextView) view);

        } else {
            Toast.makeText(this, R.string.first_country, Toast.LENGTH_SHORT).show();
        }
    }

    public EditLayoutBinding getRootElement() {
        return rootElement;
    }
}
