package com.example.rigot.socialapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class MyAccount extends AppCompatActivity implements View.OnClickListener{

    FirebaseAuth mAuth;
    FirebaseUser user;
    String userID;
    private ValueEventListener listener;
    private FirebaseStorage storage;
    private DatabaseReference mDatabase;
    ImageView backButton, editFirstNameButton, editLastNameButton, editGenderButton, editProfilePicButton, profilePic;
    TextView firstNameText, lastNameText, genderText;
    Uri target;
    Button deleteButton;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        editFirstNameButton = (ImageView)findViewById(R.id.editFirstNameButton);
        editLastNameButton = (ImageView)findViewById(R.id.editLastNameButton);
        editGenderButton = (ImageView)findViewById(R.id.editGenderButton);
        backButton = (ImageView)findViewById(R.id.backButtonMyAccount);
        editProfilePicButton = (ImageView)findViewById(R.id.editProfilePicButton);
        profilePic = (ImageView)findViewById(R.id.profilePicture);
        firstNameText = (TextView)findViewById(R.id.firstNameEditMyAccount);
        lastNameText = (TextView)findViewById(R.id.lastNameEditMyAccount);
        genderText = (TextView)findViewById(R.id.genderEditMyAccount);
        deleteButton = (Button)findViewById(R.id.acctDeleteButton);
        sharedPref = getApplicationContext().getSharedPreferences("loginHistory", MODE_PRIVATE);

        if(getIntent()!=null){
            userID = getIntent().getStringExtra("key");
        }

        mDatabase = FirebaseDatabase.getInstance().getReference().child("profiles").child(userID);

        loadInformation();

        //set on click listeners
        backButton.setOnClickListener(this);
        editFirstNameButton.setOnClickListener(this);
        editLastNameButton.setOnClickListener(this);
        editGenderButton.setOnClickListener(this);
        editProfilePicButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();

        if(i == R.id.backButtonMyAccount){ //back button clicked
            finish();
        }else if(i == R.id.editFirstNameButton){    //edit first name
            AlertDialog.Builder alert = new AlertDialog.Builder(MyAccount.this);
            alert.setTitle("Edit First Name");
            final EditText name = new EditText(MyAccount.this);
            alert.setView(name);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mDatabase.child("firstName").setValue(name.getText().toString());
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });

            alert.show();
        }else if(i == R.id.editLastNameButton){
            AlertDialog.Builder alert = new AlertDialog.Builder(MyAccount.this);
            alert.setTitle("Edit Last Name");
            final EditText name = new EditText(MyAccount.this);
            alert.setView(name);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mDatabase.child("lastName").setValue(name.getText().toString());
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });

            alert.show();
        }else if(i == R.id.editGenderButton){
            final CharSequence[] genders = {"Male", "Female"};
            AlertDialog.Builder alert = new AlertDialog.Builder(MyAccount.this);
            int selectedIndex = -1;
            if(genderText.getText().toString().equals("Male"))
                selectedIndex = 0;
            else if(genderText.getText().toString().equals("Female"))
                selectedIndex = 1;
            alert.setSingleChoiceItems(genders, selectedIndex, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int choice) {
                    mDatabase.child("gender").setValue(genders[choice]);
                }
            }).setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).show();
        }else if(i == R.id.editProfilePicButton){

            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 0);

        }else if(i == R.id.acctDeleteButton){
            AlertDialog.Builder alert = new AlertDialog.Builder(MyAccount.this)
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String provider = user.getProviders().get(0);
                            if(provider.equals("facebook.com")){
                                //remove the shared preferences if the user deletes their account linked to facebook
                                Log.d("demo", String.valueOf(sharedPref.getInt("fb", 0)));
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.remove("fb");
                                editor.commit();
                            }else if(provider.equals("google.com")){
                                //remove the shared preferences if the user deletes their account linked to google
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.remove("goog");
                                editor.commit();
                            }

                            //delete all albums
                            DatabaseReference albumsRef = FirebaseDatabase.getInstance().getReference().child("albums").child(userID);
                            albumsRef.setValue(null);

                            //create reference to this user's chats
                            final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference().child("chats").child(userID);
                            Log.d("demo", chatRef.toString());

                            //below we get the IDs
                            chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot chattingWith : dataSnapshot.getChildren()){
                                        //delete every conversation where this user appears on others' accounts
                                        DatabaseReference tempRef = FirebaseDatabase.getInstance().getReference("chats/" + chattingWith.getKey() + "/" + userID);
                                        tempRef.setValue(null);

                                    }

                                    //delete all of the user's chats
                                    chatRef.setValue(null);

                                    //finally, delete the user's profile from database and from the authenticated users
                                    mDatabase.setValue(null);
                                    user.delete();

                                    finish();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

            alert.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            target = data.getData();
            profilePic.setImageURI(target);
            String path = "profilePics/" + user.getUid() + "/" + UUID.randomUUID() + ".jpg";

            //set imagePath in User profile
            mDatabase.child("imagePath").setValue(path);

            StorageReference imageRef = storage.getReference(path); //path to store the image

            //below we upload the image to firebase storage
            profilePic.setDrawingCacheEnabled(true);
            profilePic.buildDrawingCache();
            Bitmap bitmap = profilePic.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] dataBytes = baos.toByteArray();

            UploadTask uploadTask = imageRef.putBytes(dataBytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(MyAccount.this, "Picture Updated.", Toast.LENGTH_SHORT).show();
                }
            });


        }
    }

    public void loadInformation(){
        FirebaseDatabase.getInstance().getReference().child("profiles").child(userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user information
                        FirebaseUser fbaseUser = mAuth.getCurrentUser();
                        User user = dataSnapshot.getValue(User.class);
                        if(user!=null){
                            String fName = user.firstName;
                            String lName = user.lastName;
                            firstNameText.setText(fName);
                            lastNameText.setText(lName);

                            String provider = fbaseUser.getProviders().get(0);

                            if(provider.equals("facebook.com") || provider.equals("google.com")){
                                if(user.gender!=null){
                                    genderText.setText(user.gender);
                                }else{
                                    genderText.setText("");
                                }

                                String imagePathStartsWith = user.imagePath.substring(0, 8);

                                //check to see if the user has set their own profile image or if it's the default from their provider (google, facebook)
                                if(imagePathStartsWith.equals("https://")){ //if default, load image with picasso
                                    Picasso.with(MyAccount.this)
                                            .load(user.imagePath)
                                            .into(profilePic);
                                }else{  //if user has set their own profile pic, load image from storage using Glide

                                    StorageReference ref = storage.getReference(user.getImagePath());
                                    Log.d("demo", ref.toString());
                                    Glide.with(MyAccount.this)
                                            .using(new FirebaseImageLoader())
                                            .load(ref)
                                            .into(profilePic);
                                }

                            }else{
                                genderText.setText(user.gender);

                                String path = user.getImagePath();
                                StorageReference ref = storage.getReference(path);
                                Glide.with(MyAccount.this)
                                        .using(new FirebaseImageLoader())
                                        .load(ref)
                                        .into(profilePic);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(MyAccount.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
            super.onStart();

        ValueEventListener changeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get User object and use the values to update the UI
                User user = dataSnapshot.getValue(User.class);
                FirebaseUser fbaseUser = mAuth.getCurrentUser();

                if(user!=null){
                    firstNameText.setText(user.firstName);
                    lastNameText.setText(user.lastName);

                    //get the provider that was used to log in (facebook or google)
                    String provider = fbaseUser.getProviders().get(0);

                    //if the provider was google or facebook
                    if(provider.equals("google.com") || provider.equals("facebook.com")){
                        if(user.gender!=null){  //there is a potential that the gender was not set (null)
                            genderText.setText(user.gender);
                        }else{
                            genderText.setText("");
                        }

                        Picasso.with(MyAccount.this)    //load the profile picture using Picasso
                                .load(user.imagePath)
                                .into(profilePic);
                    }else{  //the user was created directly with email and password
                        genderText.setText(user.gender);
                        StorageReference ref = storage.getReference(user.getImagePath());

                        Glide.with(MyAccount.this)  //load the picture using Glide
                                .using(new FirebaseImageLoader())
                                .load(ref)
                                .into(profilePic);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MyAccount.this, "Failed to load user data.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        mDatabase.addValueEventListener(changeListener);
        //add the change listener when activity starts

        //store reference so we can remove listener when activity stops
        listener = changeListener;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDatabase.removeEventListener(listener);
    }
}
