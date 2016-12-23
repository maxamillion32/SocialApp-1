package com.example.rigot.socialapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by rigot on 11/18/2016.
 */

public class AlbumAdapter extends ArrayAdapter<Album> {
    int mResource;
    Context mContext;
    List<Album> albums;

    public AlbumAdapter(Context context, int resource, List<Album> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
        this.albums = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {  //not recycled view
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.ivImage = (ImageView)convertView.findViewById(R.id.imageViewAlbumImg);
            holder.tvAlbumName = (TextView)convertView.findViewById(R.id.textViewAlbumName);
            convertView.setTag(holder);
        }

        holder = (ViewHolder)convertView.getTag();
        Album album = albums.get(position);

        //below we display the album thumbnail (first image) and title of the album
        Picasso.with(mContext).load(album.getImages().get(0)).fit().into(holder.ivImage);
        holder.tvAlbumName.setText(album.getTitle());

        return convertView;
    }

    static class ViewHolder{
        ImageView ivImage;
        TextView tvAlbumName;
    }
}
