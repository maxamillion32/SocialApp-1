package com.example.rigot.socialapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by rigot on 11/18/2016.
 */

public class UsersAdapter extends ArrayAdapter<User>{
    int mResource;
    Context mContext;
    List<User> users;
    private FirebaseStorage storage = FirebaseStorage.getInstance();


    public UsersAdapter(Context context, int resource, List<User> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
        this.users = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;  //create ViewHolder instance
        if (convertView == null) {  //not a recycled view
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.ivProfileImg = (ImageView) convertView.findViewById(R.id.imageViewRowUserImg);
            holder.tvName = (TextView)convertView.findViewById(R.id.textViewRowUserName);
            convertView.setTag(holder); //set holder in convertview tag
        }

        holder = (ViewHolder) convertView.getTag();

        User user = users.get(position);
        holder.tvName.setText(user.getFirstName() + " " + user.getLastName());
        if (user.getImagePath() != null && !user.getImagePath().isEmpty()) {
            String imagePathStartsWith = user.getImagePath().substring(0,8);
            //image path is for external source (google, facebook)
            if(imagePathStartsWith.equals("https://")){
                Picasso.with(mContext).load(user.getImagePath()).into(holder.ivProfileImg);
            }else{  //in this case, image path is from firebase storage
                StorageReference ref = storage.getReference(user.getImagePath());
                Glide.with(mContext).using(new FirebaseImageLoader()).load(ref).into(holder.ivProfileImg);
            }
        }

        return convertView;
    }

    static class ViewHolder{
        TextView tvName;
        ImageView ivProfileImg;
    }
}
