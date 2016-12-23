package com.example.rigot.socialapp;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by rigot on 11/18/2016.
 */

public class PhotosAdapter extends ArrayAdapter<Uri> {
    int mResource;
    Context mContext;
    List<Uri> uris;

    public PhotosAdapter(Context context, int resource, List<Uri> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
        this.uris = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {  //if not recycled view
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource, parent, false);
            holder.ivImage = (ImageView)convertView.findViewById(R.id.imageViewAlbumImg);
            holder.tvAlbumName = (TextView)convertView.findViewById(R.id.textViewAlbumName);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.ivImage.setImageURI(uris.get(position));
        holder.tvAlbumName.setVisibility(View.GONE);

        return convertView;
    }

    static class ViewHolder{
        TextView tvAlbumName;
        ImageView ivImage;
    }
}

