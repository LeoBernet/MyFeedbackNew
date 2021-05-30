package com.android.leobernet.myfeedback.act;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.leobernet.myfeedback.R;
import com.android.leobernet.myfeedback.accounthelper.AccountHelper;
import com.android.leobernet.myfeedback.adapter.DataSender;
import com.android.leobernet.myfeedback.adapter.PostAdapter;
import com.android.leobernet.myfeedback.databinding.ActivityMainBinding;
import com.android.leobernet.myfeedback.databinding.MainContentBinding;
import com.android.leobernet.myfeedback.databinding.NavHeaderMainBinding;
import com.android.leobernet.myfeedback.db.DbManager;
import com.android.leobernet.myfeedback.db.NewPost;
import com.android.leobernet.myfeedback.dialogs.SignDialog;
import com.android.leobernet.myfeedback.filter.FilterAct;
import com.android.leobernet.myfeedback.filter.FilterManager;
import com.android.leobernet.myfeedback.utils.MyConstants;
import com.google.android.gms.ads.AdRequest;
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

public class MainActivity extends AdsViewActivity implements
        NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener, DataSender {
    private ActivityMainBinding rootBinding;
    private MainContentBinding mainContent;
    private NavHeaderMainBinding navHeader;
    private FirebaseAuth mAuth;
    private PostAdapter.OnItemClickCustom onItemClickCustom;
    private PostAdapter postAdapter;
    private DbManager dbManager;
    public static String MAUTH = "";
    public String current_cat = MyConstants.DIF_CAT;
    private final int EDIT_RES = 15;
    private AccountHelper accountHelper;
    private MenuItem newAdItem, myAdsItem, myFavsItem;
    private SharedPreferences pref;
    private SignDialog signDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootBinding = ActivityMainBinding.inflate(getLayoutInflater());
        mainContent = rootBinding.mainContent;
        navHeader = NavHeaderMainBinding.inflate(getLayoutInflater(), rootBinding.navView, false);
        setContentView(rootBinding.getRoot());
        addAds(mainContent.adView);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbManager.onResume(pref);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) Picasso.get().load(account.getPhotoUrl()).into(navHeader.imPhoto);
        showFilterDialog();
        resumeCat();
    }

    private void resumeCat() {
        switch (current_cat) {
            case MyConstants.MY_ADS:
                dbManager.getMyAds(dbManager.getMyAdsNode());
                break;
            case MyConstants.MY_FAVS:
                dbManager.getMyAds(dbManager.getMyFavAdsNode());
                break;
            default:
                dbManager.getDataFromDb(current_cat, "");
        }
    }

    private void init() {
        pref = getSharedPreferences(MyConstants.MAIN_PREF, MODE_PRIVATE);
        setOnItemClickCustom();
        mAuth = FirebaseAuth.getInstance();
        accountHelper = new AccountHelper(mAuth, this);
        signDialog = new SignDialog(mAuth, this, accountHelper);
        dbManager = new DbManager(this);
        initRcView();
        initNavView();
        initToolbar();
        initDrawer();
        setNavViewStyle();
        postAdapter.setDbManager(dbManager);

        mainContent.filterDialogLayout.imCloseFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterManager.clearFilter(pref);
                mainContent.filterDialogLayout.filterHideLayout.setVisibility(View.GONE);
                dbManager.clearFilter();
            }
        });
    }

    private void initRcView() {
        mainContent.rcView.setLayoutManager(new LinearLayoutManager(this));
        List<NewPost> arrayPost = new ArrayList<>();
        postAdapter = new PostAdapter(arrayPost, this, onItemClickCustom);
        mainContent.rcView.setAdapter(postAdapter);
    }

    private void initNavView() {
        myAdsItem = rootBinding.navView.getMenu().findItem(R.id.id_my_ads);
        myFavsItem = rootBinding.navView.getMenu().findItem(R.id.id_fav);
        rootBinding.navView.addHeaderView(navHeader.getRoot());
        rootBinding.navView.setNavigationItemSelectedListener(this);

    }

    private void initToolbar() {
        mainContent.toolbar.inflateMenu(R.menu.main_menu);
        newAdItem = mainContent.toolbar.getMenu().findItem(R.id.new_ad);
        SearchView searchView = (SearchView) mainContent.toolbar.getMenu().findItem(R.id.main_search).getActionView();
        searchView.setOnQueryTextListener(this);
        onToolbarItemClick();
    }

    private void initDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, rootBinding.drawerLayout, mainContent.toolbar, R.string.toggle_open, R.string.toggle_close);
        rootBinding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setNavViewStyle() {
        Menu menu = rootBinding.navView.getMenu();
        MenuItem categoryAccountItem = menu.findItem(R.id.accountCatId);
        MenuItem categoryAds = menu.findItem(R.id.categoryAds);
        // Код для смены цвета текста в меню... nav_view
        SpannableString sp = new SpannableString(categoryAccountItem.getTitle());
        SpannableString sp2 = new SpannableString(categoryAds.getTitle());
        sp.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.black)), 0, sp.length(), 0);
        sp2.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.black)), 0, sp2.length(), 0);
        categoryAccountItem.setTitle(sp);
        categoryAds.setTitle(sp2);
    }

    private void showFilterDialog() {
        String filter = pref.getString(MyConstants.TEXT_FILTER, "empty");
        String orderBy = pref.getString(MyConstants.ORDER_BY__FILTER, "empty");
        if (filter.equals("empty")) {
            mainContent.filterDialogLayout.filterHideLayout.setVisibility(View.GONE);
        } else {
            mainContent.filterDialogLayout.filterHideLayout.setVisibility(View.VISIBLE);
            mainContent.filterDialogLayout.tvFilterInfo.setText(FilterManager.getFilterText(filter));
        }
    }

   /* private void setOnScrollListener(){
        rcView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (!rcView.canScrollVertically(1)){

                 /* dbManager.getDataFromDb(current_cat,postAdapter.getMainList()
                            .get(postAdapter.getMainList().size() -1).getTime());

                  rcView.scrollToPosition(0);*/

                /*}else if (!rcView.canScrollVertically(-1)){

                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }*/

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
                        Picasso.get().load(account.getPhotoUrl()).into(navHeader.imPhoto);
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
                navHeader.tvEmail.setText(R.string.host);
            } else {
                newAdItem.setVisible(true);
                myAdsItem.setVisible(true);
                myFavsItem.setVisible(true);
                navHeader.tvEmail.setText(currentUser.getEmail());
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
                mainContent.toolbar.setTitle("Мои объявления");
                dbManager.getMyAds(dbManager.getMyAdsNode());
                break;
            case id_my_fav:
                current_cat = MyConstants.MY_FAVS;
                mainContent.toolbar.setTitle("Избранные");
                dbManager.getMyAds(dbManager.getMyFavAdsNode());
                break;
            case id_dif:
                current_cat = MyConstants.DIF_CAT;
                mainContent.toolbar.setTitle("Разное");
                dbManager.getDataFromDb(current_cat, "");
                break;
            case id_good_car_ads:
                current_cat = getResources().getStringArray(R.array.category_spinner)[0];
                mainContent.toolbar.setTitle(current_cat);
                dbManager.getDataFromDb(current_cat, "");
                break;
            case id_normal_pc_ads:
                current_cat = getResources().getStringArray(R.array.category_spinner)[1];
                mainContent.toolbar.setTitle(current_cat);
                dbManager.getDataFromDb(current_cat, "");
                break;
            case id_bed_smartphone_ads:
                current_cat = getResources().getStringArray(R.array.category_spinner)[2];
                mainContent.toolbar.setTitle(current_cat);
                dbManager.getDataFromDb(current_cat, "");
                break;
            case id_verybed_dm_ads:
                current_cat = getResources().getStringArray(R.array.category_spinner)[3];
                mainContent.toolbar.setTitle(current_cat);
                dbManager.getDataFromDb(current_cat, "");
                break;
            case id_sign_up:
                signDialog.showSignDialog(R.string.sign_up, R.string.sign_up_button, 0);
                break;
            case id_sign_in:
                signDialog.showSignDialog(R.string.sign_in, R.string.sign_in_button, 1);
                break;
            case id_sign_out:
                accountHelper.signOut();
                navHeader.imPhoto.setImageResource(android.R.color.transparent);
                break;
        }
        rootBinding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onToolbarItemClick() {
        mainContent.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.new_ad) {
                    if (mAuth.getCurrentUser() != null) {
                        if (mAuth.getCurrentUser().isEmailVerified()) {
                            Intent i = new Intent(MainActivity.this, EditActivity.class);
                            startActivityForResult(i, EDIT_RES);
                        } else {
                            accountHelper.showDialogNotVerified(R.string.alert, R.string.mail_not_verified);
                        }
                    }
                } else if (item.getItemId() == R.id.filter) {
                    Intent i = new Intent(MainActivity.this, FilterAct.class);
                    startActivity(i);
                } else if (item.getItemId() == R.id.private_policy) {
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
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //Это что бы искал по каждой букве. Тогда сверху строчку нужно удалить из TextSubmit...
        dbManager.setSearchText(newText);
        dbManager.getDataFromDb(current_cat, "");
        return true;
    }

    @Override
    public void onDataRecived(List<NewPost> listData) {
        Collections.reverse(listData);
        postAdapter.updateAdapter(listData);
    }
}