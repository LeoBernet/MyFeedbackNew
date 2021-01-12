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
import com.android.leobernet.myfeedback.EditActivity;
import com.android.leobernet.myfeedback.MainActivity;
import com.android.leobernet.myfeedback.db.FavPathItem;
import com.android.leobernet.myfeedback.db.NewPost;
import com.android.leobernet.myfeedback.R;
import com.android.leobernet.myfeedback.ShowLayoutActivity;
import com.android.leobernet.myfeedback.db.OnFavRecivedListener;
import com.android.leobernet.myfeedback.utils.MyConstants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolderData> implements OnFavRecivedListener {

    public static final String TAG = "MyLog";
    private List<NewPost> mainPostList;
    private Context context;
    private OnItemClickCustom onItemClickCustom;
    private DbManager dbManager;
    private List<FavPathItem> favList;

    public PostAdapter(List<NewPost> arrayPost, Context context, OnItemClickCustom onItemClickCustom)
    {
        this.mainPostList = arrayPost;
        this.context = context;
        this.onItemClickCustom = onItemClickCustom;
        favList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ads,parent,false);
        return new ViewHolderData(view,onItemClickCustom);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderData holder, int position) {

        holder.setData(mainPostList.get(position));
        setFavIfSelected(holder);
    }

    @Override
    public int getItemCount() {
        return mainPostList.size();
    }

    @Override
    public void onFavRecived(List<FavPathItem> items) {
        favList.clear();
        favList.addAll(items);
        notifyDataSetChanged();
    }


    public class ViewHolderData extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        public String favPath;
        private TextView tvTitle,tvAddress,tvDisc,tvTotalViews;
        private ImageView imAds;
        private LinearLayout edit_layout;
        private ImageButton deleteButton,editButton,imFav;
        private OnItemClickCustom onItemClickCustom;

        public ViewHolderData(@NonNull View itemView,OnItemClickCustom onItemClickCustom) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle1);
            tvTotalViews = itemView.findViewById(R.id.tvTotalViews);
            tvAddress = itemView.findViewById(R.id.tvAddressText);
            tvDisc = itemView.findViewById(R.id.tvDisc1);
            imAds = itemView.findViewById(R.id.imAds);
            edit_layout = itemView.findViewById(R.id.edit_layout);
            deleteButton = itemView.findViewById(R.id.imDeleteItem);
            editButton = itemView.findViewById(R.id.imEditItem);
            imFav = itemView.findViewById(R.id.imFav);
            this.onItemClickCustom = onItemClickCustom;
            itemView.setOnClickListener(this);
        }

        public void setData(final NewPost newPost) {
            if (newPost.getUid().equals(MainActivity.MAUTH))
            {
                edit_layout.setVisibility(View.VISIBLE);
            }
            else
            {
                edit_layout.setVisibility(View.GONE);
            }
            Picasso.get().load(newPost.getImageId()).into(imAds);

            favPath = newPost.getCat() +"/" + newPost.getKey() +"/" + newPost.getUid() + "/" + "feedback";

            tvTitle.setText(newPost.getTitle());
            tvAddress.setText(newPost.getAddress());
            String textDisc;
            if (newPost.getDisc().length() > 80) {
            textDisc = newPost.getDisc().substring(0,80) + "...";
            }
            else
            {
                textDisc = newPost.getDisc();
            }

            tvDisc.setText(textDisc);
            tvTotalViews.setText(newPost.getTotal_views());

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    deleteDialog(newPost,getAdapterPosition());
                }
            });

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent i = new Intent(context, EditActivity.class);
                    i.putExtra(MyConstants.NEW_POST_INTENT,newPost);
                    i.putExtra(MyConstants.EDIT_STATE,true);
                    context.startActivity(i);
                }
            });

            imFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dbManager.updateFav(favPath);

                }
            });
        }

        @Override
        public void onClick(View v)
        {
            NewPost newPost = mainPostList.get(getAdapterPosition());

            dbManager.updateTotalViews(newPost);

            int totalViews = Integer.parseInt(newPost.getTotal_views());
            totalViews++;
            newPost.setTotal_views(String.valueOf(totalViews));
            Intent i = new Intent(context, ShowLayoutActivity.class);
            i.putExtra(MyConstants.NEW_POST_INTENT,newPost);

            context.startActivity(i);
            onItemClickCustom.onItemSelected(getAdapterPosition());
        }
    }

    private void deleteDialog (final NewPost newPost ,final int position)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.Theme_AppCompat_DayNight_Dialog_Alert);
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

    public interface OnItemClickCustom
    {
       void onItemSelected(int position);
    }
    public void updateAdapter(List<NewPost> listData)
    {
        mainPostList.clear();
        mainPostList.addAll(listData);
        notifyDataSetChanged();
    }
    public void setDbManager(DbManager dbManager)
    {
        this.dbManager = dbManager;
        dbManager.setOnFavRecivedListener(this);
    }

    private void setFavIfSelected(ViewHolderData holder){
        boolean isFav = false;
        for (FavPathItem item: favList){
            if(item.getFavPath().equals(holder.favPath)){
                isFav = true;
                break;
            }
        }
        if (isFav){
            holder.imFav.setImageResource(R.drawable.ic_fav_select);
        }else {
            holder.imFav.setImageResource(R.drawable.ic_fav_not_select);
        }
            
    }
    public List<FavPathItem> getFavList()
    {
        return favList;
    }

    public void clearAdapter(){
        mainPostList.clear();
        notifyDataSetChanged();
    }
}
