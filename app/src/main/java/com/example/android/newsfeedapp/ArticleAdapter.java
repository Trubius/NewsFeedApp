package com.example.android.newsfeedapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    private static final String LOG_TAG = ArticleAdapter.class.getSimpleName();

    private Context mContext;
    private List<Article> mArticleList;

    public ArticleAdapter (Context context, List<Article> list){
        mContext = context;
        mArticleList = list;
    }

    @NonNull
    @Override
    public ArticleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleAdapter.ViewHolder holder, int position) {
        final Article article = mArticleList.get(position);

        holder.mThumbView.setImageBitmap(formatImageFromBitmap(article.getThumbnail()));
        holder.mTitleView.setText(article.getTitle());
        holder.mDateView.setText(formatTime(article.getPublished()));
        holder.mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });
    }

    @Override
    public int getItemCount() {
        return mArticleList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mItemView;
        private ImageView mThumbView;
        private TextView mTitleView;
        private TextView mDateView;

        public ViewHolder(View view) {
            super(view);
            mItemView = view;
            mThumbView = (ImageView) view.findViewById(R.id.thumbnail);
            mTitleView = (TextView) view.findViewById(R.id.title);
            mDateView = (TextView) view.findViewById(R.id.published);
        }
    }

    private Bitmap formatImageFromBitmap(Bitmap bitmap){

        Bitmap thumbnail;

        if (bitmap == null){
            thumbnail = BitmapFactory.decodeResource(mContext.getResources(), R.id.thumbnail);
        } else {
            thumbnail = bitmap;
        }
        return thumbnail;
    }

    private String formatTime(final String time){

        String newTime = "N/A";

        if ((time != null) && (!time.isEmpty())){
            try {
                SimpleDateFormat currentFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                SimpleDateFormat newFormat = new SimpleDateFormat("dd.MM.yyyy - HH:mm");
                newTime = newFormat.format(currentFormat.parse(time));
            } catch (ParseException e) {
                newTime = "N/A";
                Log.e(LOG_TAG, "Error while parsing time format", e);
            }
        }
        return newTime;
    }
}
