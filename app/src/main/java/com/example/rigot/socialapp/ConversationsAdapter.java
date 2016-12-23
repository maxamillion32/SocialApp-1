package com.example.rigot.socialapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by rigot on 11/20/2016.
 */

public class ConversationsAdapter extends ArrayAdapter<String>{
    Context mContext;
    ArrayList<String> userIDS;
    ArrayList<User> listOfUsersChattingWith;
    int mResource;
    DatabaseReference mDatabase, mDatabaseChat;
    FirebaseStorage storage;
    FirebaseUser loggedInUser;
    User user;

    public ConversationsAdapter(Context context, int resource, ArrayList<String> objects) {
        super(context, resource, objects);
        this.mContext=context;
        this.userIDS = objects;
        this.mResource = resource;
        storage = FirebaseStorage.getInstance();
        listOfUsersChattingWith = new ArrayList<>();
        loggedInUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.conversations_row, parent, false);
            holder = new ViewHolder();
            holder.profilePic = (ImageView)convertView.findViewById(R.id.conversationProfilePic);
            holder.userName = (TextView)convertView.findViewById(R.id.conversationUserName);
            holder.isRead = (ImageView)convertView.findViewById(R.id.imageViewIsRead);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }

        //set reference to where the user information is stored
        mDatabase = FirebaseDatabase.getInstance().getReference().child("profiles").child(userIDS.get(position));
        //read the user information below
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);   //retrieve user object
                user.setuId(userIDS.get(position)); //set the users id
                listOfUsersChattingWith.add(user);  //add it to the list of users

                holder.userName.setText(user.getFirstName() + " " + user.getLastName());

                if (user.getImagePath() != null && !user.getImagePath().isEmpty()) {
                    String imagePathStartsWith = user.getImagePath().substring(0,8);
                    //image starts with https so we load with picasso
                    if(imagePathStartsWith.equals("https://")){
                        Picasso.with(mContext).load(user.getImagePath()).fit().into(holder.profilePic);
                    }else{  //in this case, image path is from firebase storage
                        StorageReference ref = storage.getReference(user.getImagePath());
                        Glide.with(mContext).using(new FirebaseImageLoader()).load(ref).into(holder.profilePic);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseChat = FirebaseDatabase.getInstance().getReference().child("chats").child(loggedInUser.getUid()).child(userIDS.get(position));

        mDatabaseChat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ChatMessage message;
                //iterate over each message
                for (DataSnapshot databaseMessage : dataSnapshot.getChildren()) {
                    message = databaseMessage.getValue(ChatMessage.class);
                    if(!message.isRead()){
                        holder.isRead.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        holder.userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start activity where we can chat with the user
                Intent intent = new Intent(mContext, ChatWindow.class);
                intent.putExtra("user", listOfUsersChattingWith.get(position));  //pass the user to the ChatWindow activity
                mContext.startActivity(intent);
            }
        });



        return convertView;
    }

    static class ViewHolder{
        ImageView profilePic, isRead;
        TextView userName;
    }
}
