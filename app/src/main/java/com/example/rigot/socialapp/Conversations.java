package com.example.rigot.socialapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Conversations extends AppCompatActivity {

    DatabaseReference mDatabase, allUsers;
    FirebaseAuth mAuth;
    ArrayList<String> conversations;
    ConversationsAdapter adapter;
    ImageView backButton;
    ListView conversationsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser loggedInUser = mAuth.getCurrentUser();
        allUsers = FirebaseDatabase.getInstance().getReference().child("profiles");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("chats").child(loggedInUser.getUid());
        backButton = (ImageView)findViewById(R.id.conversationsBackButton);
        conversationsList = (ListView)findViewById(R.id.lvConversations);


        //add value event listener so we can check to see what conversations the current user is a part of
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                conversations = new ArrayList<>();
                String userID;
                for(DataSnapshot conversation : dataSnapshot.getChildren()){
                    userID = conversation.getKey();
                    conversations.add(userID);  //add the IDs of users who logged in user has conversations with
                }

                if(conversations.size()>0){
                    adapter = new ConversationsAdapter(Conversations.this, R.layout.conversations_row, conversations);
                    conversationsList.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
