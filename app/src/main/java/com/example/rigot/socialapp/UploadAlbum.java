package com.example.rigot.socialapp;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.UUID;

public class UploadAlbum extends AppCompatActivity {

    ImageView ivAddImg;
    private ArrayList<Uri> uris;
    private PhotosAdapter adapter;
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    EditText etAlbumName;
    Button btnUpload, buttonCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_album);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("albums").child(user.getUid());
        GridView gvUploadPhotos = (GridView) findViewById(R.id.gvUploadPhotos);
        ivAddImg = (ImageView) findViewById(R.id.imageViewAddPhoto);
        etAlbumName = (EditText)findViewById(R.id.editTextAlbumName);
        btnUpload = (Button)findViewById(R.id.buttonUploadAlbum);
        buttonCancel = (Button)findViewById(R.id.buttonUploadCancel);
        uris = new ArrayList<>();


        //photos adapter
        adapter = new PhotosAdapter(this, R.layout.albums_grid, uris);
        adapter.setNotifyOnChange(true);
        gvUploadPhotos.setAdapter(adapter);

        //user adds a picture
        ivAddImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);
            }
        });

        //upload button clicked
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etAlbumName.getText().length() > 0) {
                    final DatabaseReference reference = mDatabase.child(etAlbumName.getText().toString());
                    for (Uri uri : uris) {
                        //for each uri, set path where to store images
                        String path = "albums/" + user.getUid() + "/" + etAlbumName.getText().toString().replace(" ", "_") + "/" + UUID.randomUUID() + ".jpg";
                        UploadTask uploadTask = storage.getReference(path).putFile(uri);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Toast.makeText(UploadAlbum.this, "One of the images failed to upload", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                reference.push().setValue(downloadUrl.toString());
                            }
                        });
                    }
                    Toast.makeText(UploadAlbum.this, "Album created successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(UploadAlbum.this, "Please provide album name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //user has chosen a picture to add to album
        if (resultCode == RESULT_OK) {
            Uri targetUri = data.getData();
            uris.add(targetUri);    //add uri to uri arrayList
            adapter.notifyDataSetChanged();
        }
    }
}
