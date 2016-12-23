package com.example.rigot.socialapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FacebookAuthProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener{

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    GoogleApiClient mGoogleApiClient;
    private static final int GOOGLE_SIGN_IN = 9001;
    GoogleSignInAccount googleAccount;
    SignInButton googleSignInButton;
    Button buttonLogin, buttonSignUp;
    EditText emailText;
    EditText passwordText;
    private CallbackManager mCallbackManager;
    ProgressDialog progDialog;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        buttonSignUp = (Button) findViewById(R.id.buttonLoginSignUp);
        googleSignInButton = (SignInButton)findViewById(R.id.google_sign_in);
        emailText = (EditText)findViewById(R.id.editTextLoginEmail);
        passwordText = (EditText)findViewById(R.id.editTextLoginPwd);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        sharedPref = getApplicationContext().getSharedPreferences("loginHistory", MODE_PRIVATE);

        //Configure google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //checks to see if there is currently a user logged in
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Intent intent = new Intent(MainActivity.this, HomeFeed.class);
                    startActivity(intent);
                    finish();
                }
            }
        };

        //facebook login
        mCallbackManager = CallbackManager.Factory.create();
        final LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                signInWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        //add on click listeners
        buttonLogin.setOnClickListener(this);
        buttonSignUp.setOnClickListener(this);
        googleSignInButton.setOnClickListener(this);

    }

    //handle all click events
    @Override
    public void onClick(View view) {
        int i = view.getId();

        //log in button clicked
        if(i == R.id.buttonLogin){
            if(emailText.getText().length() > 0){
                if(passwordText.getText().length() >= 6){
                    //sign in with email and password
                    signInEmailAndPass(emailText.getText().toString(), passwordText.getText().toString());
                }else{
                    Toast.makeText(MainActivity.this, "Password must be 6 characters", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(MainActivity.this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
            }
        }else if(i == R.id.buttonLoginSignUp){  //sign up button clicked
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        }else if(i == R.id.google_sign_in){ //log in with Google
            signInWithGoogle();
        }
    }

    //when the activity starts
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);  //add authStateListener
    }

    //when the activity stops
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);   //remove authStateListener
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //for when user chooses to sign in with Google credentials
        if(requestCode == GOOGLE_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            //check to see if succesfully logged in
            if(result.isSuccess()){

                //retrieve the google account
                googleAccount = result.getSignInAccount();
                firebaseAuthWithGoogle(googleAccount);

            }else
                Toast.makeText(MainActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();



        }else{  //for facebook logins
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    //sign in with email and password
    public void signInEmailAndPass(String email, String pass){
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful())
                    Toast.makeText(MainActivity.this, "Login was not successful", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //sign in with google
    public void signInWithGoogle(){
        Intent googleSignInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(googleSignInIntent, GOOGLE_SIGN_IN);
    }

    //sign in with facebook
    public void signInWithFacebook(AccessToken token){
        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Authentication failed. Email may be taken.", Toast.LENGTH_SHORT).show();
                        }else {

                            //retrieve sharedpreferences to determine if user has already signed in with facebook before
                            int previousSession = sharedPref.getInt("fb", 0);

                            //in this case, this is the first time the user is logging in with facebook
                            if(previousSession==0){
                                User dbUser = null;
                                Uri picUri;
                                FirebaseUser user = mAuth.getCurrentUser();

                                for (UserInfo profile : user.getProviderData()) {
                                    String [] name = profile.getDisplayName().split(" ");
                                    //set user variables
                                    picUri = profile.getPhotoUrl();
                                    dbUser = new User(name[0], name[1], null, picUri.toString());
                                }
                                //push facebook user to the database
                                mDatabase.child("profiles").child(user.getUid()).setValue(dbUser);

                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putInt("fb", 1);
                                editor.commit();
                            }


                        }
                    }
                });
    }

    //Authorize Google account with Firebase
    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }else{

                            //retrieve sharedpreferences to determine if user has already signed in with google before
                            int previousSession = sharedPref.getInt("goog", 0);

                            if(previousSession==0){

                                //store shared preferences to know that the user has now logged in with google
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putInt("goog", 1);
                                editor.commit();

                                progDialog = new ProgressDialog(MainActivity.this);
                                progDialog.setMessage("Creating Account");
                                progDialog.show();

                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                String[] names = googleAccount.getDisplayName().split(" ");
                                User dbUser =new User(names[0], names[1], null, googleAccount.getPhotoUrl().toString());
                                //create user in firebase database
                                mDatabase.child("profiles").child(firebaseUser.getUid()).setValue(dbUser);

                                progDialog.dismiss();
                            }

                        }
                    }
                });
    }

    //for when there is a connection failure
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
