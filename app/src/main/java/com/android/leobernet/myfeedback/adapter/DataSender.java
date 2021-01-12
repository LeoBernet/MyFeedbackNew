package com.android.leobernet.myfeedback.adapter;

import com.android.leobernet.myfeedback.db.NewPost;

import java.util.List;

public interface DataSender {
    public void onDataRecived(List<NewPost>listData);
}
