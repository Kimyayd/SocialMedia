package com.kimyayd.youpost;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.kimyayd.youpost.login.LoginActivity;

public class Splashscreen extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mAuth = FirebaseAuth.getInstance();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mAuth.getCurrentUser() != null){
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{

                    Toast.makeText(Splashscreen.this, "True", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent( getApplicationContext(), LoginActivity.class));
                    finish();
                }

            }
        },3000);

    }

}
