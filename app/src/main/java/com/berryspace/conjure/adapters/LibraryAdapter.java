package com.berryspace.conjure.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.berryspace.conjure.R;
import com.berryspace.conjure.models.Album;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.MyViewHolder> {
    private ArrayList<Album> mDataset;
    public Context context;
    private static final String TAG="LibraryAdapter";

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public TextView albumName;
        public ImageView imageView;
        public TextView year;
        public TextView artistName;

        public MyViewHolder(ConstraintLayout view) {
            super(view);
            cardView = (CardView) view.getViewById(R.id.library_album_view);
            imageView = (ImageView) cardView.getChildAt(0);
            albumName = (TextView) view.getViewById(R.id.album_name);
            artistName = (TextView) view.getViewById(R.id.artist_name);
            year = (TextView) view.getViewById(R.id.album_year);
        }
    }

    public LibraryAdapter(ArrayList<Album> dataset) {
        mDataset = dataset;
    }

    @NonNull
    @Override
    public LibraryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ConstraintLayout view = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.library_item_album, parent, false);
        MyViewHolder vh = new MyViewHolder(view);
        context = view.getContext();
        return vh;
     }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.albumName.setText(mDataset.get(position).getName());
        holder.artistName.setText(mDataset.get(position).getArtist());
        holder.year.setText(mDataset.get(position).getYear());
        Picasso.with(context).load(mDataset.get(position).getImageUrl()).into(holder.imageView);
     }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}