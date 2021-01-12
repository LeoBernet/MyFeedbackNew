package com.android.leobernet.myfeedback;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.leobernet.myfeedback.adapter.ImageAdapter;
import com.android.leobernet.myfeedback.db.DbManager;
import com.android.leobernet.myfeedback.db.NewPost;
import com.android.leobernet.myfeedback.screens.ChooseImagesActivity;
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

    private final int MAX_UPLOAD_IMAGE_SIZE = 1920;
    private StorageReference mStorageRef;
    private String[] uploadUri = new String[3];
    private String[] uploadNewUri = new String[3];
    private Spinner spinner;
    private DatabaseReference dRef;
    private FirebaseAuth mAuth;
    private EditText edTitle, edName, edAddress, edDisc;
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
    private TextView tvImagesCounter;
    private ViewPager vp;
    private List<Bitmap> bitmapArrayList;
    private ImagesManager imagesManager;
    private boolean isImagesLoaded = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);
        init();
    }

    private void init() {

        imagesManager = new ImagesManager(this, this);
        tvImagesCounter = findViewById(R.id.tvImagesCounter);
        imagesUris = new ArrayList<>();
        bitmapArrayList = new ArrayList<>();
        vp = findViewById(R.id.view_pager);
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

        uploadUri[0] = "empty";
        uploadUri[1] = "empty";
        uploadUri[2] = "empty";

        pd = new ProgressDialog(this);
        pd.setMessage("Идёт загрузка...");
        edTitle = findViewById(R.id.edTittle);
        edName = findViewById(R.id.edName);
        edAddress = findViewById(R.id.edAddress);
        edDisc = findViewById(R.id.edDisc);

        spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource
                (this, R.array.category_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
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
        if (newPost == null)return;

        edTitle.setText(newPost.getTitle());
        edName.setText(newPost.getName());
        edAddress.setText(newPost.getAddress());
        edDisc.setText(newPost.getDisc());
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
        tvImagesCounter.setText(dataText);

    }

    private void uploadImage() {

        if (load_image_counter < uploadUri.length) {

            if (!uploadUri[load_image_counter].equals("empty")) {
                Bitmap bitMap = bitmapArrayList.get(load_image_counter);


                ByteArrayOutputStream out = new ByteArrayOutputStream();
                assert bitMap != null;

                bitMap.compress(Bitmap.CompressFormat.JPEG, 20, out);
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
                            savePost();
                            Toast.makeText(EditActivity.this,
                                    "Фото загружено!",
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
            savePost();
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
                            updatePost();
                        }

                    }
                });

            }


            if (bitMap == null) return;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitMap.compress(Bitmap.CompressFormat.JPEG, 30, out);
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
                        updatePost();
                    }


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        } else {

            updatePost();
        }

    }

    public void onClickSavePost(View view) {
        if (isImagesLoaded) {
            pd.show();
            if (!edit_state) {
                uploadImage();
            } else {
                if (is_image_update) {
                    uploadUpdateImage();
                } else {
                    updatePost();
                }
            }
        }else
        {
            Toast.makeText(this, "Пожалуйста подождите, изображение загружается...", Toast.LENGTH_SHORT).show();
        }


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
                    dataText = vp.getCurrentItem() + 1 + "/" + imagesUris.size();
                } else {
                    dataText = 0 + "/" + imagesUris.size();
                }
                tvImagesCounter.setText(dataText);
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


    private void updatePost() {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getUid() != null) {
            dRef = FirebaseDatabase.getInstance().getReference(DbManager.MY_ADS_PATH);
            NewPost post = new NewPost();
            post.setImageId(uploadUri[0]);
            post.setIm_id2(uploadUri[1]);
            post.setIm_id3(uploadUri[2]);
            post.setTitle(edTitle.getText().toString());
            post.setName(edName.getText().toString());
            post.setAddress(edAddress.getText().toString());
            post.setDisc(edDisc.getText().toString());
            post.setKey(temp_key);
            post.setCat(temp_cat);
            post.setTime(temp_time);
            post.setUid(temp_uid);
            post.setTotal_views(temp_total_views);
            dRef.child(temp_key).child(mAuth.getUid()).child("feedback").setValue(post)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(EditActivity.this,
                                    "Отзыв загружен!",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        }
    }

    private void savePost() {
        dRef = FirebaseDatabase.getInstance().getReference(DbManager.MY_ADS_PATH);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getUid() != null) {
            String key = dRef.push().getKey();
            NewPost post = new NewPost();

            post.setImageId(uploadUri[0]);
            post.setIm_id2(uploadUri[1]);
            post.setIm_id3(uploadUri[2]);
            post.setTitle(edTitle.getText().toString());
            post.setName(edName.getText().toString());
            post.setAddress(edAddress.getText().toString());
            post.setDisc(edDisc.getText().toString());
            post.setKey(key);
            post.setCat(spinner.getSelectedItem().toString());
            post.setTime(String.valueOf(System.currentTimeMillis()));
            post.setUid(mAuth.getUid());
            post.setTotal_views("0");

            if (key != null) dRef.child(key).child(mAuth.getUid()).child("feedback").setValue(post);
            Intent i = new Intent();
            i.putExtra("cat", spinner.getSelectedItem().toString());
            setResult(RESULT_OK, i);
        }
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
}
