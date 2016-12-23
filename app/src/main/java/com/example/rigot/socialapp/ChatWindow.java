package com.example.rigot.socialapp;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class ChatWindow extends AppCompatActivity implements View.OnClickListener, ChatsAdapter.iActivity {

    private FirebaseUser loggedInUser;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseReceiver;
    private ImageView ivChatImg;
    FirebaseStorage storage;
    TextView tvUserName;
    ImageView galleryButton, sendButton;
    EditText chatMsg;
    User user;
    ListView chatsList;
    ArrayList<ChatMessage> chats;
    ChatsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);

        mAuth = FirebaseAuth.getInstance();

        storage = FirebaseStorage.getInstance();
        chatsList = (ListView)findViewById(R.id.lvChats);
        tvUserName = (TextView)findViewById(R.id.textViewChatUserName);
        galleryButton = (ImageView)findViewById(R.id.imageViewChatGallery);
        sendButton = (ImageView)findViewById(R.id.imageViewChatSend);
        chatMsg = (EditText)findViewById(R.id.editTextChatMsg);
        ivChatImg = (ImageView)findViewById(R.id.imageViewChatImg);

        //the logged in user
        loggedInUser = mAuth.getCurrentUser();
        //the user that the logged in user will be chatting with
        user = (User) getIntent().getSerializableExtra("user");

        //set name of person user is chatting with
        tvUserName.setText(user.getFirstName() + " " + user.getLastName());

        //database references for both users
        mDatabase = FirebaseDatabase.getInstance().getReference().child("chats").child(loggedInUser.getUid()).child(user.getuId());
        mDatabaseReceiver = FirebaseDatabase.getInstance().getReference().child("chats").child(user.getuId()).child(loggedInUser.getUid());

        //set click listeners
        galleryButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);

        //add a listener on the database so whenever there is a new message
        //we can reflect that on the user's screen with the use of a listview and adapter
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chats = new ArrayList<>();
                ChatMessage message;
                //iterate over each message
                for(DataSnapshot databaseMessage : dataSnapshot.getChildren()){
                    message = databaseMessage.getValue(ChatMessage.class);
                    message.setMsgId(databaseMessage.getKey());
                    chats.add(message);
                }
                if(chats.size()>0){
                    adapter = new ChatsAdapter(ChatWindow.this, R.layout.chat_row, chats);
                    chatsList.setAdapter(adapter);
                    //set the focus of the chat on the last message that was received
                    chatsList.setSelection(adapter.getCount()-1);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();

        //user wants to select an image to send
        if(i == R.id.imageViewChatGallery){
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 0);
        }else if(i == R.id.imageViewChatSend){  //send message
            if(chatMsg.getText().length() == 0 && ivChatImg.getTag() == null){  //make sure there is content to the message
                Toast.makeText(ChatWindow.this, "Please type a message or select an image to send", Toast.LENGTH_SHORT).show();
            } else {
                //set the message details below
                final ChatMessage chatMessage = new ChatMessage();
                chatMessage.setSentByUid(loggedInUser.getUid());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                chatMessage.setDate(sdf.format(new Date()));
                chatMessage.setTextMsg(chatMsg.getText().toString());

                //check to see if the user wants to send an image
                if(ivChatImg.getTag() != null){
                    //set the path where the image path will be saved
                    String path = "chats/" + loggedInUser.getUid() + "/" + user.getuId() + "/" + UUID.randomUUID() + ".jpg";
                    UploadTask uploadTask = storage.getReference(path).putFile((Uri)ivChatImg.getTag());
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(ChatWindow.this, "Image failed to upload", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();

                            chatMessage.setImagePath(downloadUrl.toString());

                            chatMessage.setRead(true);
                            mDatabase.push().setValue(chatMessage);
                            chatMessage.setRead(false);
                            mDatabaseReceiver.push().setValue(chatMessage);

                            chatMsg.setText("");
                            ivChatImg.setTag(null);
                            ivChatImg.setImageURI(null);
                            ivChatImg.setVisibility(View.GONE);
                        }
                    });
                } else {
                    chatMessage.setRead(true);
                    mDatabase.push().setValue(chatMessage);
                    chatMessage.setRead(false);
                    mDatabaseReceiver.push().setValue(chatMessage);

                    chatMsg.setText("");
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //display the image that the user wants to send
        if (resultCode == RESULT_OK) {
            Uri targetUri = data.getData();
            ivChatImg.setImageURI(targetUri);
            ivChatImg.setTag(targetUri);
            ivChatImg.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void deleteChat(String msgId) {
        mDatabase.child(msgId).removeValue();
    }

    @Override
    public void updateIsRead(String msgId) {
        mDatabase.child(msgId).child("read").setValue(true);
    }
}
