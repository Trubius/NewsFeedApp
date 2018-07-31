package com.example.android.newsfeedapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    private static final String LOG_TAG = ArticleAdapter.class.getSimpleName();

    private Context mContext;
    private List<Article> mArticleList;

    public ArticleAdapter(Context context, List<Article> list) {
        mContext = context;
        mArticleList = list;
        setHasStableIds(true);
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
        holder.mContent.setText(article.getContent());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.mContent.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        }
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

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mItemView;
        private ImageView mThumbView;
        private TextView mTitleView;
        private TextView mCategory;
        private TextView mDateView;
        private TextView mAuthor;
        private TextView mContent;

        public ViewHolder(View view) {
            super(view);
            mItemView = view;
            mThumbView = (ImageView) view.findViewById(R.id.thumbnail);
            mTitleView = (TextView) view.findViewById(R.id.title);
            mCategory = (TextView) view.findViewById(R.id.category);
            mDateView = (TextView) view.findViewById(R.id.published);
            mAuthor = (TextView) view.findViewById(R.id.author);
            mContent = (TextView) view.findViewById(R.id.content);
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
        long date;
        long now;
        long difference;
        CharSequence ago;

        if ((time != null) && (!time.isEmpty())) {
            try {
                SimpleDateFormat currentFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                currentFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                date = currentFormat.parse(time).getTime();
                now = System.currentTimeMillis();
                difference = now - date;
                ago = DateUtils.getRelativeTimeSpanString(date,now,DateUtils.FORMAT_ABBREV_ALL);
                if (difference < 60000){
                    // If the article was published less than 1 minute ago
                    newTime = "Just now";
                } else {
                    newTime = ago.toString();
                }
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
        mArticleList.addAll(articles);
        notifyItemRangeInserted(0, mArticleList.size() - 1);
    }
}
