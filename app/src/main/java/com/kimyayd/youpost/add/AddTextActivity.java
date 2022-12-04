package com.kimyayd.youpost.add;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kimyayd.youpost.R;
import com.kimyayd.youpost.models.Post;
import com.kimyayd.youpost.models.Text;
import com.kimyayd.youpost.utils.FirebaseMethods;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class AddTextActivity extends AppCompatActivity {
   private FloatingActionButton fab;
   private EditText text;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private int textCount = 1;

    public static final String TAG="AddTextActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_text);
    setupFirebaseAuth();
    fab=findViewById(R.id.pickTextFab);
    text=findViewById(R.id.edittext);

    fab.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: Attempting to post new text");
            //upload the image to firebase
            Toast.makeText(AddTextActivity.this, "Attempting to post new text", Toast.LENGTH_SHORT).show();
            String post = text.getText().toString();

            addNewText(post);


        }
    });
    }
    public void addNewText(String text) {
        String postID = myRef.push().getKey();
        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("caption", text);
        hashMap.put("date_created", getTimestamp());
        hashMap.put("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
        hashMap.put("post_id", postID);
        hashMap.put("post_type", "0");

        myRef.child(getString(R.string.dbname_user_texts))
                .child(user_id)
                .child("text")
                .child(postID)
                .setValue(hashMap);
        myRef.child(getString(R.string.dbname_user_posts))
                .child(user_id)
                .child("post")
                .child(postID)
                .setValue(hashMap);
        finish();
    }
    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.FRENCH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+1"));
        return sdf.format(new Date());
    }

//    public void newText(String textType,final String post,final int count){
//        Log.d(TAG, "uploadText: attempting to uplaod new text.");
//            String textID = myRef.push().getKey();
//            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//            String tags = StringManipulation.getTags(post);
//            Text text = new Text();
//            text.setCaption(tags);
//            text.setDate_created(getTimestamp());
//            text.setTags(tags);
//            text.setUser_id(user_id);
//            text.setText_id(textID);
//
//            myRef.child(getString(R.string.dbname_texts))
//                    .child(textID)
//                    .setValue(post);
//        }


        private void someMethod() {
        /*
            Step 1)
            Create a data model for Text
            Step 2)
            Add properties to the Text Objects (caption, date,photo_id, tags, user_id)
            Step 3)
            Count the number of photos that the user already has.
            Step 4)
            a) Upload the photo to Firebase Storage
            b) insert into 'photos' node
            c) insert into 'user_photos' node
         */
        }


    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        Log.d(TAG, "onDataChange: text count: " + textCount);

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

        Toast.makeText(AddTextActivity.this, "Everything is okay!!", Toast.LENGTH_SHORT).show();

        myRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //retrieve user information from the database
             //   textCount = mFirebaseMethods.getImageCount(dataSnapshot);
                Log.d(TAG, "onDataChange: text count: " + textCount);

                //retrieve images for the user in question

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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