package com.berryspace.conjure;

import android.content.Context;
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

public class AlbumSearchResultsAdapter extends RecyclerView.Adapter<AlbumSearchResultsAdapter.MyViewHolder>{
    private ArrayList<Album> mDataset;
    public Context context;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public TextView albumName;
        public ImageView imageView;
        public TextView year;
        private static String TAG = "AlbumSearchResultsAdapter";

        public MyViewHolder(ConstraintLayout view) {
            super(view);
            cardView = (CardView) view.getViewById(R.id.search_album_view);
            imageView = (ImageView) cardView.getChildAt(0);
            albumName = (TextView) view.getViewById(R.id.search_album_name);
            year = (TextView) view.getViewById(R.id.search_album_year);
        }
    }

    public AlbumSearchResultsAdapter(ArrayList<Album> dataset) {
        mDataset = dataset;
    }

    @Override
    public AlbumSearchResultsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {
        ConstraintLayout view = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item_album, parent, false);
        MyViewHolder vh = new MyViewHolder(view);
        context = view.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Log.i("SearchResultsAdapter", mDataset.get(position).getName());
        holder.albumName.setText((CharSequence) mDataset.get(position).getName());
        holder.year.setText((CharSequence) (mDataset.get(position).getYear()));
        Picasso.with(context).load(mDataset.get(position).getImageUrl()).into(holder.imageView);

        holder.cardView.setOnClickListener(v -> {

        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}