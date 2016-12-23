package com.example.rigot.socialapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.server.converter.ConverterWrapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFeed extends AppCompatActivity implements View.OnClickListener{

    private DatabaseReference mDatabase;
    ArrayList<User> users;
    ArrayList<User> searchResult;
    UsersAdapter adapter;
    ListView lvUsers;
    FirebaseAuth mAuth;
    ImageView logoutButton, mailButton, acctButton;
    Button searchButton;
    EditText etSearch;
    AlertDialog.Builder alertDialog;
    FirebaseAuth.AuthStateListener mAuthStateListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_feed);

        mAuth = FirebaseAuth.getInstance();
        lvUsers = (ListView)findViewById(R.id.lvUsers);
        searchButton = (Button)findViewById(R.id.buttonSearchUser);
        etSearch = (EditText)findViewById(R.id.editTextSearchUser);
        logoutButton = (ImageView)findViewById(R.id.logoutButton);
        mailButton = (ImageView)findViewById(R.id.mailButton);
        acctButton = (ImageView)findViewById(R.id.accountButton);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("profiles");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users = new ArrayList<User>();
                User user;
                for (DataSnapshot singleSnapShot : dataSnapshot.getChildren()){
                    user = singleSnapShot.getValue(User.class); //get each user from the database
                    user.setuId(singleSnapShot.getKey());
                    users.add(user);    //add each user to the users ArrayList
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //set on click listeners
        mailButton.setOnClickListener(this);
        acctButton.setOnClickListener(this);
        logoutButton.setOnClickListener(this);
        searchButton.setOnClickListener(this);


        lvUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent detailsIntent = new Intent(HomeFeed.this, UserDetails.class);
                detailsIntent.putExtra("user", searchResult.get(position));
                startActivity(detailsIntent);
            }
        });

        //listen for any change in users logged in
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user==null){ //for when a user logs out
                    Toast.makeText(HomeFeed.this, "Logged out", Toast.LENGTH_SHORT).show();
                    Intent home = new Intent(HomeFeed.this, MainActivity.class);
                    startActivity(home);
                    finish();
                }
            }
        };

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        //respond to menu item selection
        switch (item.getItemId()) {
            case R.id.uploadPhotos:
                startActivity(new Intent(this, UploadAlbum.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();

        if(i == R.id.logoutButton){     //logout button
            logout();
        }else if(i == R.id.accountButton){  //account button
            FirebaseUser user = mAuth.getCurrentUser();
            Intent accountIntent = new Intent(HomeFeed.this, MyAccount.class);
            accountIntent.putExtra("key", user.getUid());
            startActivity(accountIntent);
        }else if(i == R.id.mailButton){     //messages button
            Intent conversationsIntent = new Intent(HomeFeed.this, Conversations.class);
            startActivity(conversationsIntent);
        }else if(i == R.id.buttonSearchUser){   //search user
            if(etSearch.getText().toString().isEmpty())
                Toast.makeText(HomeFeed.this, "Please enter search query", Toast.LENGTH_SHORT).show();
            else {
                String[] searchWords = etSearch.getText().toString().split(" ");
                searchResult = new ArrayList<>();
                if (searchWords[0] != null && !searchWords[0].isEmpty()) {
                    for (User user : users) {
                        if (user.getFirstName().toLowerCase().contains(searchWords[0].toLowerCase()) || user.getLastName().toLowerCase().contains(searchWords[0].toLowerCase())) {
                            searchResult.add(user);
                            continue;
                        }
                        if (searchWords.length > 1) {
                            if (user.getFirstName().toLowerCase().contains(searchWords[1].toLowerCase()) || user.getLastName().toLowerCase().contains(searchWords[1].toLowerCase())) {
                                searchResult.add(user);
                            }
                        }
                    }
                }

                if(searchResult.size() > 0){
                    adapter = new UsersAdapter(HomeFeed.this, R.layout.users_row, searchResult);
                    lvUsers.setAdapter(adapter);
                } else {
                    Toast.makeText(HomeFeed.this, "No user found", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void logout(){
        alertDialog = new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAuth.signOut();
                    }
                }).setNegativeButton("No", null);

        alertDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }
}
