package com.android.leobernet.myfeedback;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.leobernet.myfeedback.accounthelper.AccountHelper;
import com.android.leobernet.myfeedback.adapter.DataSender;
import com.android.leobernet.myfeedback.adapter.PostAdapter;
import com.android.leobernet.myfeedback.db.DbManager;
import com.android.leobernet.myfeedback.db.NewPost;
import com.android.leobernet.myfeedback.filter.FilterAct;
import com.android.leobernet.myfeedback.filter.FilterManager;
import com.android.leobernet.myfeedback.utils.MyConstants;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener {


    private NavigationView nav_view;
    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;
    private TextView userEmail,tvFilterInfo;
    private AlertDialog dialog;
    private Toolbar toolbar;
    private PostAdapter.OnItemClickCustom onItemClickCustom;
    public RecyclerView rcView;
    private PostAdapter postAdapter;
    private DataSender dataSender;
    private DbManager dbManager;
    public static String MAUTH = "";
    public String current_cat = MyConstants.DIF_CAT;
    private final int EDIT_RES = 15;
    private AdView adView;
    private AccountHelper accountHelper;
    private ImageView imPhoto;
    private ImageButton imCloseFilter;
    private MenuItem newAdItem,myAdsItem,myFavsItem;
    private SharedPreferences pref;
    private CardView filterHideContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addAds();
        init();
        setOnScrollListener();

    }

    @Override
    protected void onResume() {
        super.onResume();
        dbManager.onResume(pref);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) Picasso.get().load(account.getPhotoUrl()).into(imPhoto);
        if (adView != null) {
            adView.resume();
        }
            showFilterDialog();
            dbManager.getDataFromDb(current_cat,"",false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adView != null) {
            adView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adView != null) {
            adView.destroy();
        }
    }

    private void init() {

        pref = getSharedPreferences(MyConstants.MAIN_PREF,MODE_PRIVATE);
        filterHideContainer = findViewById(R.id.filterHideLayout);
        tvFilterInfo = findViewById(R.id.tvFilterInfo);
        imCloseFilter = findViewById(R.id.imCloseFilter);
        setOnItemClickCustom();
        rcView = findViewById(R.id.rcView);
        rcView.setLayoutManager(new LinearLayoutManager(this));
        List<NewPost> arrayPost = new ArrayList<>();
        postAdapter = new PostAdapter(arrayPost, this, onItemClickCustom);
        rcView.setAdapter(postAdapter);

        nav_view = findViewById(R.id.nav_view);
        myAdsItem = nav_view.getMenu().findItem(R.id.id_my_ads);
        myFavsItem = nav_view.getMenu().findItem(R.id.id_fav);
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main_menu);
        newAdItem = toolbar.getMenu().findItem(R.id.new_ad);
        SearchView searchView = (SearchView) toolbar.getMenu().findItem(R.id.main_search).getActionView();
        searchView.setOnQueryTextListener(this);

        onToolbarItemClick();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.toggle_open, R.string.toggle_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        nav_view.setNavigationItemSelectedListener(this);
        imPhoto = nav_view.getHeaderView(0).findViewById(R.id.imPhoto);

        userEmail = nav_view.getHeaderView(0).findViewById(R.id.tvEmail);
        mAuth = FirebaseAuth.getInstance();
        accountHelper = new AccountHelper(mAuth, this);

        Menu menu = nav_view.getMenu();

        MenuItem categoryAccountItem = menu.findItem(R.id.accountCatId);
        MenuItem categoryAds = menu.findItem(R.id.categoryAds);

        // Код для смены цвета текста в меню... nav_view
        SpannableString sp = new SpannableString(categoryAccountItem.getTitle());
        SpannableString sp2 = new SpannableString(categoryAds.getTitle());
        sp.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.black)), 0, sp.length(), 0);
        sp2.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.black)), 0, sp2.length(), 0);
        categoryAccountItem.setTitle(sp);
        categoryAds.setTitle(sp2);

        getDataDB();
        dbManager = new DbManager(dataSender, this);
        postAdapter.setDbManager(dbManager);

        imCloseFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterManager.clearFilter(pref);
                filterHideContainer.setVisibility(View.GONE);
            }
        });
    }

    private void showFilterDialog(){
        String filter = pref.getString(MyConstants.TEXT_FILTER,"empty");
        String orderBy = pref.getString(MyConstants.ORDER_BY__FILTER,"empty");
        if (filter.equals("empty")){
            filterHideContainer.setVisibility(View.GONE);
        }else{
            filterHideContainer.setVisibility(View.VISIBLE);
            tvFilterInfo.setText(FilterManager.getFilterText(filter));
        }
    }

    private void setOnScrollListener(){
        rcView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (!rcView.canScrollVertically(1)){

                 /* dbManager.getDataFromDb(current_cat,postAdapter.getMainList()
                            .get(postAdapter.getMainList().size() -1).getTime());

                  rcView.scrollToPosition(0);*/

                }else if (!rcView.canScrollVertically(-1)){

                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    private void getDataDB() {
        dataSender = new DataSender() {
            @Override
            public void onDataRecived(List<NewPost> listData) {
                Collections.reverse(listData);
                postAdapter.updateAdapter(listData);
            }
        };
    }

    private void setOnItemClickCustom() {
        onItemClickCustom = new PostAdapter.OnItemClickCustom() {
            @Override
            public void onItemSelected(int position) {

            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case EDIT_RES:
                if (resultCode == RESULT_OK && data != null) {

                    current_cat = data.getStringExtra("cat");
                }
                break;
            case AccountHelper.GOOGLE_SIGN_IN_CODE:
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null) {
                        Picasso.get().load(account.getPhotoUrl()).into(imPhoto);
                        accountHelper.signInFirebaseGoogle(account.getIdToken(), 0);
                    }

                } catch (ApiException e) {
                    e.printStackTrace();
                }

                break;
            case AccountHelper.GOOGLE_SIGN_IN_LINK_CODE:
                Task<GoogleSignInAccount> task2 = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task2.getResult(ApiException.class);
                    if (account != null)
                        accountHelper.signInFirebaseGoogle(account.getIdToken(), 1);

                } catch (ApiException e) {
                    e.printStackTrace();
                }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI();
    }

    public void updateUI() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.isAnonymous()) {
                newAdItem.setVisible(false);
                myFavsItem.setVisible(false);
                myAdsItem.setVisible(false);
                userEmail.setText(R.string.host);
            }else{
                newAdItem.setVisible(true);
                myAdsItem.setVisible(true);
                myFavsItem.setVisible(true);
                userEmail.setText(currentUser.getEmail());
            }

            MAUTH = mAuth.getUid();
            onResume();
        } else {
           accountHelper.Anonimous();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        final int id_my_ads = R.id.id_my_ads;
        final int id_my_fav = R.id.id_fav;
        final int id_dif = R.id.id_dif;
        final int id_good_car_ads = R.id.id_good_car_ads;
        final int id_normal_pc_ads = R.id.id_normal_pc_ads;
        final int id_bed_smartphone_ads = R.id.id_bed_smartphone_ads;
        final int id_verybed_dm_ads = R.id.id_verybed_dm_ads;
        final int id_sign_up = R.id.id_sign_up;
        final int id_sign_in = R.id.id_sign_in;
        final int id_sign_out = R.id.id_sign_out;
        postAdapter.isStartPage = true;

        switch (id) {
            case id_my_ads:
                current_cat = MyConstants.MY_ADS;
                dbManager.getMyAds(dbManager.getMyAdsNode());
                break;
            case id_my_fav:
                current_cat = MyConstants.MY_FAVS;
                dbManager.getMyAds(dbManager.getMyFavAdsNode());
                break;
            case id_dif:
                current_cat = MyConstants.DIF_CAT;
                dbManager.getDataFromDb(current_cat,"",false);
                break;
            case id_good_car_ads:
                current_cat = getResources().getStringArray(R.array.category_spinner)[0];
                dbManager.getDataFromDb(current_cat,"",false);
                break;
            case id_normal_pc_ads:
                current_cat = getResources().getStringArray(R.array.category_spinner)[1];
                dbManager.getDataFromDb(current_cat,"",false);
                break;
            case id_bed_smartphone_ads:
                current_cat = getResources().getStringArray(R.array.category_spinner)[2];
                dbManager.getDataFromDb(current_cat,"",false);
                break;
            case id_verybed_dm_ads:
                current_cat = getResources().getStringArray(R.array.category_spinner)[3];
                dbManager.getDataFromDb(current_cat,"",false);
                break;
            case id_sign_up:
                signUpDialog(R.string.sign_up, R.string.sign_up_button, R.string.google_sign_up, 0);
                break;
            case id_sign_in:
                signUpDialog(R.string.sign_in, R.string.sign_in_button, R.string.google_sign_in, 1);
                break;
            case id_sign_out:
                accountHelper.signOut();
                imPhoto.setImageResource(android.R.color.transparent);
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signUpDialog(int title, int buttonTitle, int b2Title, final int index) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.sign_up_layout, null);
        dialogBuilder.setView(dialogView);
        final TextView titleTextView = dialogView.findViewById(R.id.tvAlertTitle);
        final TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        titleTextView.setText(title);
        final Button signUpEmail = dialogView.findViewById(R.id.buttonSignUp);
        final SignInButton signUpGoogle = dialogView.findViewById(R.id.bSignGoogle);
        final Button forgetPassword = dialogView.findViewById(R.id.bForgetPassword);

        switch (index){
            case 0:
                forgetPassword.setVisibility(View.GONE);
                break;
            case 1:
                forgetPassword.setVisibility(View.VISIBLE);
                break;
        }
        final EditText edEmail = dialogView.findViewById(R.id.edEmail);
        final EditText edPassword = dialogView.findViewById(R.id.edPassword);
        signUpEmail.setText(buttonTitle);
        signUpEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null) {
                        if (mAuth.getCurrentUser().isAnonymous()){
                            mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        if(index == 0)accountHelper.signUp(edEmail.getText().toString(), edPassword.getText().toString());
                                        else accountHelper.signIn(edEmail.getText().toString(), edPassword.getText().toString());
                                }
                            }
                        });
                    }
                }
                dialog.dismiss();
            }
        });
        signUpGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null) {
                   if(mAuth.getCurrentUser().isAnonymous()){
                       mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               if(task.isSuccessful())accountHelper.signInGoogle(AccountHelper.GOOGLE_SIGN_IN_CODE);
                           }
                       });
                   }
                }
                dialog.dismiss();
            }
        });
        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edPassword.isShown()){
                    
                edPassword.setVisibility(View.GONE);
                signUpEmail.setVisibility(View.GONE);
                signUpGoogle.setVisibility(View.GONE);
                titleTextView.setText(R.string.forget_password);
                forgetPassword.setText(R.string.send_reset_password);
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.setText(R.string.forget_password_message);

            } else {
                    if (!edEmail.getText().toString().equals("")){
                    FirebaseAuth.getInstance().sendPasswordResetEmail(edEmail.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(MainActivity.this, R.string.email_is_send, Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                }
                            });
                    } else {
                        Toast.makeText(MainActivity.this, R.string.email_is_empty, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        dialog = dialogBuilder.create();
        //Код который убирает задний белый фон в Алерт диалоге.
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    private void addAds() {
        MobileAds.initialize(this);
        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void onToolbarItemClick(){
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId()== R.id.new_ad){
                    if (mAuth.getCurrentUser() != null) {
                        if (mAuth.getCurrentUser().isEmailVerified()) {
                            Intent i = new Intent(MainActivity.this, EditActivity.class);
                            startActivityForResult(i, EDIT_RES);
                        } else {
                            accountHelper.showDialogNotVerified(R.string.alert, R.string.mail_not_verified);
                        }
                    }
                }else if (item.getItemId() == R.id.filter){
                    Intent i = new Intent(MainActivity.this , FilterAct.class);
                    startActivity(i);
                }else if (item.getItemId() == R.id.private_policy) {
                    Intent i = new Intent(MainActivity.this, InfoAct.class);
                    startActivity(i);
                }
                return false;
            }
        });
    }

    public FirebaseAuth getmAuth() {
        return mAuth;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        //Это что бы искал по целому слову. Тогда снизу строчку нужно удалить из TextChange...
        //dbManager.getSearchResult(query.toLowerCase());
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //Это что бы искал по каждой букве. Тогда сверху строчку нужно удалить из TextSubmit...
        dbManager.getSearchResult(newText.toLowerCase());
        return true;
    }
}