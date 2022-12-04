package com.kimyayd.youpost.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kimyayd.youpost.R;

public class ResetPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ResetPasswordActivity";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        TextView emailPassword = (TextView) findViewById(R.id.emailPassword);
        setupFirebaseAuth();

        Button sendVerificationEmail = (Button) findViewById(R.id.sendVerificationEmail);
        sendVerificationEmail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                String userEmail = emailPassword.getText().toString();
          if(isStringNull(userEmail)){

              Toast.makeText(ResetPasswordActivity.this, "Please write your valid email adress first", Toast.LENGTH_SHORT).show();
          }

          else{
              mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {

                  @Override
                  public void onComplete(@NonNull Task<Void> task) {

                      if(task.isSuccessful()){
                          Toast.makeText(ResetPasswordActivity.this, "Please check your email account", Toast.LENGTH_SHORT).show();
                          startActivity(new Intent(getApplicationContext(),LoginActivity.class));

                      }else{
                          String message=task.getException().getMessage();
                          Toast.makeText(ResetPasswordActivity.this, "Error occured: "+ message, Toast.LENGTH_SHORT).show();

                      }
                  }
              });
          }
            }
        });
    }

    private boolean isStringNull(String string) {
        Log.d(TAG, "isStringNull: checking string if null.");

        if (string.equals("")) {
            return true;
        } else {
            return false;
        }
    }



    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out");
            }
            // ...
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}