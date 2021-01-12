package com.android.leobernet.myfeedback.db;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.room.Database;

import com.android.leobernet.myfeedback.R;
import com.android.leobernet.myfeedback.adapter.DataSender;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class DbManager {
    public static final String MY_ACC_PATH = "accounts";
    public static final String MY_ADS_PATH = "main_ads_path";
    public static final String MY_FAV_PATH = "my_fav";
    public static final String MY_FAV_ADS_PATH = "my_fav_ads_path";
    private Context context;
    private Query mQuery;
    private List<NewPost> newPostList;
    private DataSender dataSender;
    private FirebaseDatabase db;
    private FirebaseStorage fs;
    private FirebaseAuth mAuth;
    private int cat_ads_counter = 0;
    private int deleteImageCounter = 0;
    private List<FavPathItem> mainListFav;
    private OnFavRecivedListener onFavRecivedListener;

    public void deleteItem(final NewPost newPost)
    {
        StorageReference sRef = null;
        switch (deleteImageCounter)
        {
            case 0:
               if (!newPost.getImageId().equals("empty")){
                   sRef = fs.getReferenceFromUrl(newPost.getImageId());
            }
               else
               {
                   deleteImageCounter++;
                   deleteItem(newPost);
               }
                break;
            case 1:
                if (!newPost.getIm_id2().equals("empty")){
                    sRef = fs.getReferenceFromUrl(newPost.getIm_id2());
                }
                else
                {
                    deleteImageCounter++;
                    deleteItem(newPost);
                }
                break;
            case 2:
                if (!newPost.getIm_id3().equals("empty")){
                    sRef = fs.getReferenceFromUrl(newPost.getIm_id3());
                }
                else
                {
                    deleteDBItem(newPost);
                    sRef = null;
                    deleteImageCounter = 0;
                }
                break;
        }

        if (sRef == null)return;

        sRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
               deleteImageCounter++;
               if (deleteImageCounter < 3)
               {
                   deleteItem(newPost);
               }
               else
               {
                   deleteImageCounter = 0;
                   deleteDBItem(newPost);
               }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Фотография не удалилась!!!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteDBItem(NewPost newPost)
    {
        DatabaseReference dbRef = db.getReference(newPost.getCat());
        dbRef.child(newPost.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, R.string.item_deleted, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Отзыв не был удалён!!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateTotalViews(final NewPost newPost)
    {
        DatabaseReference dRef =FirebaseDatabase.getInstance().getReference(MY_ADS_PATH);
        int total_views;
        try
        {
            total_views = Integer.parseInt(newPost.getTotal_views());
        }
        catch (NumberFormatException e)
        {
            total_views = 0;
        }
        total_views++;
        StatusItem statusItem = new StatusItem();
        statusItem.totalViews = String.valueOf(total_views);
        dRef.child(newPost.getKey()).child("status").setValue(statusItem);
    }

    public void updateFav(final String favPath) {

        if (mAuth.getUid() == null) return;
        DatabaseReference dRef =FirebaseDatabase.getInstance().getReference(MY_ACC_PATH);
        String key = dRef.push().getKey();

        if (key == null) return;
        boolean isFav = false;
        String keyToDelete = "";
        for (FavPathItem favPathItem : mainListFav){

            if (favPathItem.getFavPath().equals(favPath)) {
                isFav = true;
                keyToDelete = favPathItem.getKey();
            }
        }
        if (isFav){

            deleteFav(keyToDelete);

        }else {

            FavPathItem favPathItem = new FavPathItem();
            favPathItem.setKey(key);
            favPathItem.setFavPath(favPath);
            dRef.child(mAuth.getUid()).child(MY_FAV_PATH).child(key).child(MY_FAV_ADS_PATH).setValue(favPathItem);

        }

    }

    public DbManager(DataSender dataSender, Context context) {
        this.dataSender = dataSender;
        this.context = context;
        newPostList = new ArrayList<>();
        db = FirebaseDatabase.getInstance();
        fs = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mainListFav = new ArrayList<>();
    }

    public void getDataFromDb(String cat) {

        if (mAuth.getUid() !=null){

        DatabaseReference dbRef = db.getReference(MY_ADS_PATH);
        mQuery = dbRef.orderByChild(mAuth.getUid() + "/feedback/time");
        readDataUpdate();
        }

    }
    public void getMyAdsDataFromDb(String uid) {

        if (mAuth.getUid() == null)return;
        if (newPostList.size() > 0) newPostList.clear();
        DatabaseReference dbRef = db.getReference(MY_ADS_PATH);
        mQuery = dbRef.orderByChild(mAuth.getUid() + "/feedback/uid").equalTo(uid);
        readMyAdsDataUpdate();
        cat_ads_counter++;

    }

    public void readDataUpdate() {
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (newPostList.size() > 0) newPostList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    NewPost newPost = ds.getChildren().iterator().next().child("feedback").getValue(NewPost.class);
                    StatusItem statusItem = ds.child("status").getValue(StatusItem.class);

                    if (newPost != null && statusItem != null) {
                        newPost.setTotal_views(statusItem.totalViews);
                }
                    newPostList.add(newPost);
                }
                dataSender.onDataRecived(newPostList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void readMyFav(final List<FavPathItem> favList) {

        DatabaseReference dbRef = db.getReference();
        final List<NewPost> tempFavList = new ArrayList<>();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                tempFavList.clear();
                for (FavPathItem item : favList) {

                    NewPost newPost = dataSnapshot.child(item.getFavPath()).getValue(NewPost.class);
                    String[] statusArrayPath = item.getFavPath().split("/");
                    StatusItem statusItem = dataSnapshot.child(statusArrayPath[0]).
                            child(statusArrayPath[1]).child("status").getValue(StatusItem.class);

                    if(newPost != null && statusItem != null) {

                        newPost.setTotal_views(statusItem.totalViews);
                    }

                    tempFavList.add(newPost);


                }

                dataSender.onDataRecived(tempFavList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void readMyAdsDataUpdate() {
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    NewPost newPost = ds.child(mAuth.getUid() + "/feedback").getValue(NewPost.class);
                    StatusItem statusItem = ds.child("status").getValue(StatusItem.class);
// тут взял в фигурные скобки после !=null) {

                    if (newPost !=null && statusItem !=null) {
                        newPost.setTotal_views(statusItem.totalViews);
                    }
                    newPostList.add(newPost);
                }
                    dataSender.onDataRecived(newPostList);
                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void readFavs() {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference(MY_ACC_PATH);
        if (mAuth.getUid() == null)return;

        dRef.child(mAuth.getUid()).child(MY_FAV_PATH).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mainListFav.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    mainListFav.add(ds.child(MY_FAV_ADS_PATH).getValue(FavPathItem.class));
                }

                onFavRecivedListener.onFavRecived(mainListFav);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void deleteFav (String key){
        if (mAuth.getUid() == null) return;
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference(MY_ACC_PATH);
        dRef.child(mAuth.getUid()).child(MY_FAV_PATH).child(key).removeValue();
    }

    public void  setOnFavRecivedListener(OnFavRecivedListener onFavRecivedListener){
       this.onFavRecivedListener = onFavRecivedListener;
    }
}
