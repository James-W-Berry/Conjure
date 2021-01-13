package com.berryspace.conjure.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import com.berryspace.conjure.R;
import com.berryspace.conjure.models.Album;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.MyViewHolder> implements Filterable {
    private ArrayList<Album> mDataset;
    private ArrayList<Album> mAllData;
    public Context context;
    private ArrayList<Album> filteredList;
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

    @Override
    public Filter getFilter() {
        return stringFilter;
    }

    public void storeAllResults(ArrayList<Album> allData){
        mAllData = allData;
    }

    private Filter stringFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList = mAllData;
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Album item : mDataset) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mDataset = filteredList;
            notifyDataSetChanged();
        }
    };

}