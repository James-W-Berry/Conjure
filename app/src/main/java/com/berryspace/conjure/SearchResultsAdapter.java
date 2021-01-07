package com.berryspace.conjure;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.MyViewHolder>{
    private ArrayList<Artist> mDataset;
    public Context context;
    private static final String TAG="SearchResultsAdapter";

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public TextView artistName;
        public TextView genres;
        public ImageView imageView;
        public TextView followers;

        public MyViewHolder(ConstraintLayout view) {
            super(view);
            cardView = (CardView) view.getViewById(R.id.search_artist_view);
            imageView = (ImageView) cardView.getChildAt(0);
            artistName = (TextView) view.getViewById(R.id.search_artist_name);
            genres = (TextView) view.getViewById(R.id.search_artist_genres);
            followers = (TextView) view.getViewById(R.id.search_artist_followers);
        }
    }

    public SearchResultsAdapter(ArrayList<Artist> dataset) {
        mDataset = dataset;
    }

    @Override
    public SearchResultsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        ConstraintLayout view = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item_artist, parent, false);
        MyViewHolder vh = new MyViewHolder(view);
        context = view.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.artistName.setText(mDataset.get(position).getName());
        holder.genres.setText(mDataset.get(position).getGenres());
        holder.followers.setText(mDataset.get(position).getFollowers()+" followers");
        Picasso.with(context).load(mDataset.get(position).getImageUrl()).into(holder.imageView);

         holder.itemView.setOnClickListener(v -> {
            Log.i(TAG, "trying to launch AlbumSelectorActivity");
            Intent intent = new Intent (v.getContext(), AlbumSelectorActivity.class);
            intent.putExtra("id", mDataset.get(position).getId());
            intent.putExtra("name", mDataset.get(position).getName());
            v.getContext().startActivity(intent);
        });
     }

    @Override
    public int getItemCount() {
         return mDataset.size();
    }
}