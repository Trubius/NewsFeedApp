package com.example.android.newsfeedapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

    public ArticleAdapter(Context context, List<Article> list) {
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
        holder.mCategory.setText(article.getCategory());
        holder.mDateView.setText(formatTime(article.getPublished()));
        holder.mAuthor.setText(article.getAuthor());
        holder.mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = article.getUrl();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mContext.startActivity(browserIntent);
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
        private TextView mCategory;
        private TextView mDateView;
        private TextView mAuthor;

        public ViewHolder(View view) {
            super(view);
            mItemView = view;
            mThumbView = (ImageView) view.findViewById(R.id.thumbnail);
            mTitleView = (TextView) view.findViewById(R.id.title);
            mCategory = (TextView) view.findViewById(R.id.category);
            mDateView = (TextView) view.findViewById(R.id.published);
            mAuthor = (TextView) view.findViewById(R.id.author);
        }
    }

    private Bitmap formatImageFromBitmap(Bitmap bitmap) {

        Bitmap thumbnail;

        if (bitmap == null) {
            thumbnail = BitmapFactory.decodeResource(mContext.getResources(), R.id.thumbnail);
        } else {
            thumbnail = bitmap;
        }
        return thumbnail;
    }

    private String formatTime(final String time) {

        String newTime = "N/A";

        if ((time != null) && (!time.isEmpty())) {
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

    public void clear() {
        final int size = mArticleList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                mArticleList.remove(0);
            }
            notifyItemRangeRemoved(0, size);
        }
    }

    public void addAll(List<Article> articles) {
        this.mArticleList.addAll(articles);
        this.notifyItemRangeInserted(0, mArticleList.size() - 1);
    }
}
