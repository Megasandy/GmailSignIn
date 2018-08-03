package com.auth.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient  mGoogleSignInClient;
    private int RC_SIGN_IN=1;
    private Button mGmailBtn,mSignOutBtn;
    private TextView mTitle;
    private final String TAG="MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set layout
        setContentView(R.layout.activity_main);
        //create an instance of firebaseAuth
        mAuth = FirebaseAuth.getInstance();
        //assign btn and text view instances
        mGmailBtn=findViewById(R.id.id_gmail_sign_in_btn);
        mTitle=findViewById(R.id.id_sign_in_text_or_email);
        mSignOutBtn=findViewById(R.id.id_signOut_btn);

        //create google sign in client
        GoogleSignInOptions gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient=GoogleSignIn.getClient(this,gso);


        mGmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(TAG,"Sign in process has started");
                signIn();
            }
        });



        mSignOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(TAG,"Sign out process has started");
               signOut();
            }
        });
    }

    private void signOut() {
        //Firebase sign out
        mAuth.signOut();
        //Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                updateUI(null);
            }
        });
        Log.w(TAG,"Sign out process has been completed");
    }

    private void signIn() {
        Intent signInIntent=mGoogleSignInClient.getSignInIntent();
        Log.w(TAG,"Sign in intent has started");
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.w(TAG,"Sign in onActivityResult process has started");
        if((requestCode==RC_SIGN_IN)
                )
        {
            Task<GoogleSignInAccount> task=GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                Log.w(TAG,"Google sign in started");
                GoogleSignInAccount account=task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                Log.w(TAG,"FirebaseAuth with Google account has been created");
            } catch (ApiException e) {
                Log.w(TAG,"google sign in failed");
                e.printStackTrace();

            }
        }
        else
            Log.w(TAG,"Sign in onActivityResult process failed");
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        //show progress dialog
        AuthCredential credential=GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            Log.w(TAG,"Firebase sign in sucessfully completed");
                            FirebaseUser currentUser=mAuth.getCurrentUser();
                            updateUI(currentUser);
                        }
                        else {
                            updateUI(null);
                            Log.w(TAG,"firebase sign in Failed");

                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.w(TAG,"Cheaking .... Current user");
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
       updateUI(currentUser);

    }

    private void updateUI(FirebaseUser currentUser) {
        if(currentUser!=null)
        {
            findViewById(R.id.id_gmail_sign_in_btn).setVisibility(View.GONE);
            findViewById(R.id.id_signOut_btn).setVisibility(View.VISIBLE);
            mTitle.setText("User Gmail account: "+currentUser.getEmail()+"\n"+""+currentUser.getDisplayName());
            Log.w(TAG,"someone is signed in");


        }
        else {
            findViewById(R.id.id_gmail_sign_in_btn).setVisibility(View.VISIBLE);
            mTitle.setText("Sign In,You can select any following ways to sign in  ");
            findViewById(R.id.id_signOut_btn).setVisibility(View.GONE);
            Log.w(TAG,"No current user");


        }
    }



}
