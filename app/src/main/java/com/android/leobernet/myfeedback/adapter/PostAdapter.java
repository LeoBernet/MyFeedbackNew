package com.android.leobernet.myfeedback.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.leobernet.myfeedback.db.DbManager;
import com.android.leobernet.myfeedback.act.EditActivity;
import com.android.leobernet.myfeedback.act.MainActivity;
import com.android.leobernet.myfeedback.db.NewPost;
import com.android.leobernet.myfeedback.R;
import com.android.leobernet.myfeedback.act.ShowLayoutActivity;
import com.android.leobernet.myfeedback.utils.MyConstants;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolderData> {

    public static final String NEXT_PAGE = "nextPage";
    public static final String BACK_PAGE = "backPage";
    private List<NewPost> mainPostList;
    private Context context;
    private OnItemClickCustom onItemClickCustom;
    private DbManager dbManager;
    private int myViewType = 0;
    private int VIEW_TYPE_ADS = 0;
    private int VIEW_TYPE_END_BUTTON = 1;
    public boolean isStartPage = true;
    private final int NEXT_ADS_B = 1;
    private boolean isTotalViewsAdded = false;
    private boolean needClear = true;

    public PostAdapter(List<NewPost> arrayPost, Context context, OnItemClickCustom onItemClickCustom) {
        this.mainPostList = arrayPost;
        this.context = context;
        this.onItemClickCustom = onItemClickCustom;
    }

    @NonNull
    @Override
    public ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_END_BUTTON) {
            view = LayoutInflater.from(context).inflate(R.layout.end_ads_item, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_ads, parent, false);
        }
        return new ViewHolderData(view, onItemClickCustom);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderData holder, int position) {

        if (NEXT_PAGE.equals(mainPostList.get(position).getUid())) {
            holder.setNextItemData();
        } else {
            holder.setData(mainPostList.get(position));
            setFavIfSelected(holder);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mainPostList.get(position).getUid().equals(NEXT_PAGE)){
            myViewType = 1;
        } else {
            myViewType = 0;
        }
        return myViewType;
    }

    @Override
    public int getItemCount() {
        return mainPostList.size();
    }

    public class ViewHolderData extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tvTitle, tvAddress, tvDisc, tvTotalViews, tvName, tvFavCounter,tvHouseAddress;
        private ImageView imAds;
        private LinearLayout edit_layout;
        public ImageButton deleteButton, editButton, imFav;
        private OnItemClickCustom onItemClickCustom;

        public ViewHolderData(@NonNull View itemView, OnItemClickCustom onItemClickCustom) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTitle = itemView.findViewById(R.id.tvTitle1);
            tvTotalViews = itemView.findViewById(R.id.tvTotalViews);
            tvAddress = itemView.findViewById(R.id.tvAddressText);
            tvDisc = itemView.findViewById(R.id.tvDisc1);
            tvHouseAddress = itemView.findViewById(R.id.tvHouseAddress);
            imAds = itemView.findViewById(R.id.imAds);
            edit_layout = itemView.findViewById(R.id.edit_layout);
            deleteButton = itemView.findViewById(R.id.imDeleteItem);
            editButton = itemView.findViewById(R.id.imEditItem);
            imFav = itemView.findViewById(R.id.imFav);
            tvFavCounter = itemView.findViewById(R.id.tvFavCounter);
            this.onItemClickCustom = onItemClickCustom;
            itemView.setOnClickListener(this);
        }

        public void setNextItemData() {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String lastAddressTime = mainPostList.get(mainPostList.size() - 2).getAddress().toLowerCase() + "_"
                            + mainPostList.get(mainPostList.size() - 2).getTime();
                    dbManager.getDataFromDb(((MainActivity) context).current_cat,lastAddressTime);
                    isStartPage = false;
                    needClear = false;
                }
            });
        }

        public void setData(final NewPost newPost) {
            if (newPost == null) return;

            FirebaseUser user = ((MainActivity) context).getmAuth().getCurrentUser();
            if (user != null) {
                edit_layout.setVisibility(newPost.getUid().equals(user.getUid()) ? View.VISIBLE : View.GONE);
                imFav.setVisibility(user.isAnonymous() ? View.GONE : View.VISIBLE);
            }
            Picasso.get().load(newPost.getImageId()).into(imAds);

            tvTitle.setText(newPost.getTitle());
            tvFavCounter.setText(String.valueOf(newPost.getFavCounter()));
            tvAddress.setText(newPost.getAddress());
            tvName.setText(newPost.getName());
            tvHouseAddress.setText(newPost.getHouse());
            String textDisc;
            if (newPost.getDisc().length() > 80) {
                textDisc = newPost.getDisc().substring(0, 80) + "...";
            } else {
                textDisc = newPost.getDisc();
            }

            tvDisc.setText(textDisc);
            tvTotalViews.setText(newPost.getTotal_views());

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    deleteDialog(newPost, getAdapterPosition());
                }
            });

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent i = new Intent(context, EditActivity.class);
                    i.putExtra(MyConstants.NEW_POST_INTENT, newPost);
                    i.putExtra(MyConstants.EDIT_STATE, true);
                    context.startActivity(i);
                }
            });

            imFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    setFavCounter(newPost,tvFavCounter);
                    dbManager.updateFav(newPost, ViewHolderData.this);
                }
            });
        }

        private void setFavCounter(NewPost newPost, TextView tvFavCounter){
            int fCounter = Integer.parseInt(tvFavCounter.getText().toString());
            fCounter = (newPost.isFav())? --fCounter : ++fCounter;
            tvFavCounter.setText(String.valueOf(fCounter));
        }

        @Override
        public void onClick(View v) {
            NewPost newPost = mainPostList.get(getAdapterPosition());
            dbManager.updateTotalCounter(DbManager.TOTAL_VIEWS,newPost.getKey(), newPost.getTotal_views());
            int totalViews = Integer.parseInt(newPost.getTotal_views());
            totalViews++;
            newPost.setTotal_views(String.valueOf(totalViews));
            Intent i = new Intent(context, ShowLayoutActivity.class);
            i.putExtra(MyConstants.NEW_POST_INTENT, newPost);
            context.startActivity(i);
            onItemClickCustom.onItemSelected(getAdapterPosition());
        }
    }

    private void deleteDialog(final NewPost newPost, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        builder.setTitle(R.string.delete_title);
        builder.setMessage(R.string.delete_message);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dbManager.deleteItem(newPost);
                mainPostList.remove(position);
                notifyItemRemoved(position);
            }
        });
        builder.show();

    }

    public interface OnItemClickCustom {
        void onItemSelected(int position);
    }

    public void updateAdapter(List<NewPost> listData) {
        if (needClear){
            mainPostList.clear();
        }else{
            listData.remove(0);
        }

        if (listData.size() == MyConstants.ADS_LIMIT){
            NewPost tempPost = new NewPost();
            tempPost.setUid(NEXT_PAGE);
            listData.add(tempPost);
        }
        int mainArraySize = mainPostList.size() -1;
        if (mainArraySize == -1)mainArraySize = 0;
        mainPostList.addAll(mainArraySize,listData);
        if (mainArraySize == 0){
            notifyDataSetChanged();
        }else{
            notifyItemRangeChanged(mainArraySize,listData.size());
        }
        if (listData.size() < MyConstants.ADS_LIMIT -1 && mainPostList.size() > 0){
            int pos = mainPostList.size()-1;
            mainPostList.remove(pos);
            notifyItemRemoved(pos);
        }
        needClear = true;
    }

    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    private void setFavIfSelected(ViewHolderData holder) {
        if (mainPostList.get(holder.getAdapterPosition()).isFav()) {
            holder.imFav.setImageResource(R.drawable.ic_fav_select);
        } else {
            holder.imFav.setImageResource(R.drawable.ic_fav_not_select);
        }

    }

    public void clearAdapter() {
        mainPostList.clear();
        notifyDataSetChanged();
    }

    public List<NewPost> getMainList() {
        return mainPostList;
    }
}
