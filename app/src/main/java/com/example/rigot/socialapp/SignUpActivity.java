package com.example.rigot.socialapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mDatabase;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    EditText etFirstName, etLastName, etEmail, etPassword, etPassword2;
    Button buttonSignUp, buttonCancel, profilePicButton;
    String[] genders = new String[] {"Male", "Female"};
    Spinner genderSpinner;
    ImageView profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("profiles");
        mAuth = FirebaseAuth.getInstance();
        profilePic = (ImageView)findViewById(R.id.profilePicSignUp);
        genderSpinner = (Spinner)findViewById(R.id.genderSpinner);
        etFirstName = (EditText)findViewById(R.id.editTextSignUpName);
        etLastName = (EditText)findViewById(R.id.editTextSignUpLastName);
        etEmail = (EditText)findViewById(R.id.editTextSignUpEmail);
        etPassword = (EditText)findViewById(R.id.editTextSignUpPwd);
        etPassword2 = (EditText)findViewById(R.id.editTextRepeatPwd);
        buttonCancel = (Button)findViewById(R.id.buttonSignUpCancel);
        buttonSignUp = (Button)findViewById(R.id.buttonSignUp);
        profilePicButton = (Button)findViewById(R.id.setProfilePicButton);
        profilePicButton.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
        buttonSignUp.setOnClickListener(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, genders);
        genderSpinner.setAdapter(adapter);
        genderSpinner.setSelection(0);

    }


    @Override
    public void onClick(View view) {
        int i = view.getId();

        if(i == R.id.buttonSignUpCancel){   //cancel button
            Intent mainIntent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        }else if(i == R.id.setProfilePicButton){    //set profile pic button
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 0);
        }else if(i == R.id.buttonSignUp){   //sign up button
            if (etFirstName.getText().length() > 0 && etLastName.getText().length() >0 && etEmail.getText().length() > 0) {
                if (etPassword.getText().length() >= 6) {
                    if(etPassword.getText().toString().equals(etPassword2.getText().toString())) {
                        mAuth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString()).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(SignUpActivity.this, "User may exists with provided Email", Toast.LENGTH_SHORT).show();
                                } else {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {

                                        String path = "profilePics/" + user.getUid() + "/" + UUID.randomUUID() + ".jpg";
                                        StorageReference imageRef = storage.getReference(path); //path to store the image

                                        User dbUser = new User();
                                        dbUser.setFirstName(etFirstName.getText().toString());
                                        dbUser.setLastName(etLastName.getText().toString());
                                        dbUser.setGender(genderSpinner.getSelectedItem().toString());
                                        dbUser.setImagePath(path);
                                        mDatabase.child(user.getUid()).setValue(dbUser);
                                        //push the user to the database

                                        profilePic.setDrawingCacheEnabled(true);
                                        profilePic.buildDrawingCache();
                                        Bitmap bitmap = profilePic.getDrawingCache();
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                        byte[] data = baos.toByteArray();

                                        final UploadTask uploadTask = imageRef.putBytes(data);
                                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                Toast.makeText(SignUpActivity.this, "Account Created.", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                        Intent homeIntent = new Intent(SignUpActivity.this, HomeFeed.class);
                                        startActivity(homeIntent);
                                        finish();   //go to home feed
                                    }
                                }
                            }
                        });
                    } else
                        Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignUpActivity.this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SignUpActivity.this, "Please fill all details", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Uri target = data.getData();
            profilePic.setImageURI(target);
        }
    }

}
