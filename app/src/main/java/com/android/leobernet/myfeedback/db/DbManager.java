package com.android.leobernet.myfeedback.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.leobernet.myfeedback.R;
import com.android.leobernet.myfeedback.adapter.DataSender;
import com.android.leobernet.myfeedback.adapter.PostAdapter;
import com.android.leobernet.myfeedback.status.StatusItem;
import com.android.leobernet.myfeedback.utils.MyConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

    public static final String MAIN_ADS_PATH = "main_ads_path";
    public static final String MY_FAV_PATH = "my_fav";
    public static final String FAV_ADS_PATH = "fav_path";
    public static final String USER_FAV_ID = "userFavId";
    public static final String ORDER_BY_CAT_TIME = "/status/cat_time";
    public static final String ORDER_BY_TIME = "/status/filter_by_time";
    public static final String TOTAL_VIEWS = "status/totalViews";
    private Context context;
    private Query mQuery;
    private final List<NewPost> newPostList;
    private DataSender dataSender;
    private FirebaseDatabase db;
    private FirebaseStorage fs;
    private final FirebaseAuth mAuth;
    private int cat_ads_counter = 0;
    private int deleteImageCounter = 0;
    private DatabaseReference mainNode;
    private String filter;
    private String orderByFilter;

    public DbManager(DataSender dataSender, Context context) {
        this.dataSender = dataSender;
        this.context = context;
        newPostList = new ArrayList<>();
        db = FirebaseDatabase.getInstance();
        fs = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mainNode = db.getReference(MAIN_ADS_PATH);
    }

    public void onResume(SharedPreferences pref){
        filter = pref.getString(MyConstants.FILTER,"");
        orderByFilter = pref.getString(MyConstants.ORDER_BY__FILTER,"");
    }

    public void deleteItem(final NewPost newPost) {

        StorageReference sRef = null;

        switch (deleteImageCounter) {
            case 0:
                if (!newPost.getImageId().equals("empty")) {
                    sRef = fs.getReferenceFromUrl(newPost.getImageId());

                } else {
                    deleteImageCounter++;
                    deleteItem(newPost);
                }
                break;
            case 1:
                if (!newPost.getIm_id2().equals("empty")) {
                    sRef = fs.getReferenceFromUrl(newPost.getIm_id2());

                } else {
                    deleteImageCounter++;
                    deleteItem(newPost);
                }
                break;
            case 2:
                if (!newPost.getIm_id3().equals("empty")) {
                    sRef = fs.getReferenceFromUrl(newPost.getIm_id3());

                } else {
                    deleteDBItem(newPost);
                    sRef = null;
                    deleteImageCounter = 0;
                }
                break;
        }

        if (sRef == null) return;

        sRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                deleteImageCounter++;
                if (deleteImageCounter < 3) {
                    deleteItem(newPost);

                } else {
                    deleteImageCounter = 0;
                    deleteDBItem(newPost);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, R.string.image_no_delete, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteDBItem(NewPost newPost) {

        DatabaseReference dbRef = db.getReference(DbManager.MAIN_ADS_PATH);
        dbRef.child(newPost.getKey()).child("status").removeValue();
        dbRef.child(newPost.getKey()).child(mAuth.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, R.string.item_deleted, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, R.string.feedback_no_delete, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateTotalCounter(final String counterPath, String key, String counter) {

        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference(MAIN_ADS_PATH);
        int totalCounter;
        try {
            totalCounter = Integer.parseInt(counter);
        } catch (NumberFormatException e) {
            totalCounter = 0;
        }
        totalCounter++;
        dRef.child(key).child(counterPath).setValue(String.valueOf(totalCounter));
    }

    public void getMyAds(String orderBy) {

        mQuery = mainNode.orderByChild(orderBy).equalTo(mAuth.getUid());
        readDataUpdate();
    }

    public void getDataFromDb(String cat, String lastTime,boolean lastPage) {

        if (mAuth.getUid() == null) return;
        String divider = "_";
        String orderBy = (cat.equals(MyConstants.DIF_CAT)) ? ORDER_BY_TIME : ORDER_BY_CAT_TIME;
        if (cat.equals(MyConstants.DIF_CAT)) {
            divider = "";
            cat = "";
        }
        if (!orderByFilter.equals("")){
            orderBy = orderByFilter;
            divider = "";
        }
       if (!lastPage)
           mQuery = mainNode.orderByChild(orderBy).startAt(cat).endAt(cat + divider + lastTime + "\uf8ff").limitToLast(MyConstants.ADS_LIMIT);
       else
           mQuery = mainNode.orderByChild(orderBy).startAt(cat + divider + lastTime).limitToFirst(MyConstants.ADS_LIMIT);

        readDataUpdate();
    }

    public void getSearchResult(String searchText) {
        if (mAuth.getUid() == null) return;
        DatabaseReference dbRef = db.getReference(MAIN_ADS_PATH);
        mQuery = dbRef.orderByChild("/status/" + orderByFilter).startAt(filter + searchText).endAt(filter + searchText + "\uf8ff").limitToLast(MyConstants.ADS_LIMIT);
        readDataUpdate();
    }

    public void readDataUpdate() {
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (newPostList.size() > 0) newPostList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    NewPost newPost = null;

                    for (DataSnapshot ds2 : ds.getChildren()) {
                        if (newPost == null)
                            newPost = ds2.child("feedback").getValue(NewPost.class);
                    }

                    StatusItem statusItem = ds.child("status").getValue(StatusItem.class);
                    String uid = mAuth.getUid();
                    if (uid != null) {
                        String favUid = (String) ds.child(FAV_ADS_PATH).child(mAuth.getUid()).child(USER_FAV_ID).getValue();
                        if (newPost != null)
                            newPost.setFavCounter(ds.child(FAV_ADS_PATH).getChildrenCount());
                        if (favUid != null && newPost != null) newPost.setFav(true);
                    }

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

    public void updateFav(final NewPost newPost, PostAdapter.ViewHolderData holder) {

        if (newPost.isFav()) {
            deleteFav(newPost, holder);
        } else {
            addFav(newPost, holder);
        }
    }

    private void addFav(final NewPost newPost, final PostAdapter.ViewHolderData holder) {
        if (mAuth.getUid() == null) return;
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference(MAIN_ADS_PATH);

        dRef.child(newPost.getKey()).child(FAV_ADS_PATH).child(mAuth.getUid()).child(USER_FAV_ID)
                .setValue(mAuth.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    holder.imFav.setImageResource(R.drawable.ic_fav_select);
                    newPost.setFav(true);
                }
            }
        });
    }

    private void deleteFav(final NewPost newPost, final PostAdapter.ViewHolderData holder) {
        if (mAuth.getUid() == null) return;
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference(MAIN_ADS_PATH);
        dRef.child(newPost.getKey()).child(FAV_ADS_PATH).child(mAuth.getUid()).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            holder.imFav.setImageResource(R.drawable.ic_fav_not_select);
                            newPost.setFav(false);
                        }
                    }
                });
    }

    public String getMyAdsNode() {
        return mAuth.getUid() + "/feedback/uid";
    }

    public String getMyFavAdsNode() {
        return FAV_ADS_PATH + "/" + mAuth.getUid() + "/" + USER_FAV_ID;
    }
}
