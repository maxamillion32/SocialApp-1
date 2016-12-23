package com.example.rigot.socialapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
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

public class UserDetails extends AppCompatActivity {

    private FirebaseUser loggedInUser;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ArrayList<Album> albums;
    private AlbumAdapter adapter;
    ImageView ivUserImg, messageButton;
    TextView userName, gender;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        gender = (TextView)findViewById(R.id.textViewUserGender);
        userName = (TextView)findViewById(R.id.textViewUserName);
        ivUserImg = (ImageView)findViewById(R.id.imageViewUserProfile);
        loggedInUser = mAuth.getCurrentUser();
        messageButton = (ImageView)findViewById(R.id.imageViewUserMessage);
        final User user = (User) getIntent().getSerializableExtra("user");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("albums").child(user.getuId());

        final GridView gvAlbums = (GridView)findViewById(R.id.gvUserAlbums);

        //set profile image
        if (user.getImagePath() != null && !user.getImagePath().isEmpty()) {
            String path = user.getImagePath().substring(0,8);
            if(path.equals("https://")){
                Picasso.with(UserDetails.this).load(user.getImagePath()).into(ivUserImg);
            }else{
                StorageReference imageRef = storage.getReference(user.getImagePath());
                Glide.with(UserDetails.this).using(new FirebaseImageLoader()).load(imageRef).into(ivUserImg);
            }
        }

        //set some attributes
        userName.setText(user.getFirstName() + " " + user.getLastName());
        gender.setText(user.getGender());

        if(user.getuId().equals(loggedInUser.getUid()))
            messageButton.setVisibility(View.GONE);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                albums = new ArrayList<>();
                Album album;
                //iterate through all of the user's albums
                for (DataSnapshot individualAlbum : dataSnapshot.getChildren()) {
                    album = new Album();
                    album.setTitle(individualAlbum.getKey());
                    ArrayList<String> paths = new ArrayList<>();
                    //iterate through each image in the album
                    for (DataSnapshot pictures : individualAlbum.getChildren()){
                        String path = (String) pictures.getValue();
                        paths.add(path);
                    }
                    //set image paths for the album
                    album.setImages(paths);
                    albums.add(album);
                }
                adapter = new AlbumAdapter(UserDetails.this, R.layout.albums_grid, albums);
                gvAlbums.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //when a specific album is clicked, start other activity where the user may view the images
        gvAlbums.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(UserDetails.this, ViewPhotos.class);
                intent.putExtra("album", albums.get(i));
                startActivity(intent);
            }
        });

        //when message button is clicked
        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start activity where we can chat with the user
                Intent intent = new Intent(UserDetails.this, ChatWindow.class);
                intent.putExtra("user", user);  //pass the user to the ChatWindow activity
                startActivity(intent);
            }
        });


    }
}
