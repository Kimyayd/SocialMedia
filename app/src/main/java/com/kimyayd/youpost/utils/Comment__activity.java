package com.kimyayd.youpost.utils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kimyayd.youpost.R;
import com.kimyayd.youpost.message.MessageActivity;
import com.kimyayd.youpost.models.Chat;
import com.kimyayd.youpost.models.Comment;
import com.kimyayd.youpost.models.RealVideo;
import com.kimyayd.youpost.models.Video;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class Comment__activity extends AppCompatActivity {
    private static final String TAG = "Comment_activity";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    //widgets
    private ImageView mBackArrow, mCheckMark;
    private EditText mComment;
    private ListView listView;

    private ArrayList<Comment> comments;
    private Context mContext;
    private String video_id,user_id,type;
    private RelativeLayout no_comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        mContext = Comment__activity.this;
        Intent intent = getIntent();
        video_id = intent.getStringExtra("id");
        user_id = intent.getStringExtra("user_id");
        type = intent.getStringExtra("type");
        Toast.makeText(mContext, "Hi "+video_id+user_id+type, Toast.LENGTH_SHORT).show();

        mBackArrow =  findViewById(R.id.backArrow);
        mCheckMark =  findViewById(R.id.ivPostComment);
        mComment =  findViewById(R.id.comment);
        listView =  findViewById(R.id.comment_recycler);

        comments=new ArrayList<>();
        setupFirebaseAuth();
        getComments();
        setupWidgets();
    }

    private void getComments(){

            if(type.equals("0")||type.equals("1")){
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user_posts").child(user_id).child("comments").child(video_id);
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        comments.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Comment modelComment = ds.getValue(Comment.class);
                            comments.add(modelComment);
                        }
                        no_comment = findViewById(R.id.relLayout2);
                        if (!(comments.size() == 0)) {
                            no_comment.setVisibility(View.GONE);
                            Comment_Adapter commentAdapter = new Comment_Adapter(getApplicationContext(), R.layout.layout_comment, comments, user_id, video_id);//set adapter
                            listView.setAdapter(commentAdapter);
                            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                @Override
                                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Comment comment = comments.get(i);
                                    if (comment.getUser_id().equals( FirebaseAuth.getInstance().getCurrentUser().getUid())){

                                        final Dialog dialog = new Dialog(mContext);
                                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                        dialog.setContentView(R.layout.pop_thing);
                                        LinearLayout layoutCopy = dialog.findViewById(R.id.layoutCopy);
                                        LinearLayout layoutDelete = dialog.findViewById(R.id.layoutDelete);

                                        layoutCopy.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                                String message = comment.getComment();
                                                setClipboard(mContext.getApplicationContext(), message);
                                                Toast.makeText(Comment__activity.this, "Copied", Toast.LENGTH_SHORT).show();

                                            }
                                        });

                                        layoutDelete.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(view.getRootView().getContext());
                                                builder.setTitle("Delete");
                                                builder.setMessage("Are you sure you want to delete this comment ?");
                                                builder.setPositiveButton("YES", (dialogInterface, i) -> {
                                                    //delete comment
                                                    dialog.dismiss();
                                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user_posts");
                                                    ref.child(comment.getUser_id()).child("comments").child(comment.getPost_id()).child(comment.getComment_id()).removeValue();
                                                    ref.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            Toast.makeText(Comment__activity.this, "Deleted", Toast.LENGTH_SHORT).show();
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });


                                                });
                                                builder.setNegativeButton("NO", (dialogInterface, i) -> {
                                                    //dismiss dialog
                                                    dialogInterface.dismiss();
                                                });
                                                //show dialog
                                                builder.create().show();
                                            }
                                        });

                                        dialog.show();
                                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                        dialog.getWindow().setGravity(Gravity.BOTTOM);

                                    }
                                    else{
                                        String message = comment.getComment();
                                        setClipboard(mContext.getApplicationContext(), message);
                                        Toast.makeText(Comment__activity.this, "Copied", Toast.LENGTH_SHORT).show();
                                    }
                                    return true;
                                }
                            });
                        } else {
                            no_comment.setVisibility(View.VISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            else if(type.equals("2")){
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user_videos").child(user_id).child("comments").child(video_id);
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        comments.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Comment modelComment = ds.getValue(Comment.class);
                            comments.add(modelComment);
                        }
                        no_comment = findViewById(R.id.relLayout2);
                        if (!(comments.size() == 0)) {
                            no_comment.setVisibility(View.GONE);
                            Comment_Adapter commentAdapter = new Comment_Adapter(getApplicationContext(), R.layout.layout_comment, comments, user_id, video_id);//set adapter
                            listView.setAdapter(commentAdapter);
                            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                @Override
                                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Comment comment = comments.get(i);
                                    if (comment.getUser_id().equals( FirebaseAuth.getInstance().getCurrentUser().getUid())){

                                        final Dialog dialog = new Dialog(mContext);
                                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                        dialog.setContentView(R.layout.pop_thing);
                                        LinearLayout layoutCopy = dialog.findViewById(R.id.layoutCopy);
                                        LinearLayout layoutDelete = dialog.findViewById(R.id.layoutDelete);

                                        layoutCopy.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                                String message = comment.getComment();
                                                setClipboard(mContext.getApplicationContext(), message);
                                                Toast.makeText(Comment__activity.this, "Copied", Toast.LENGTH_SHORT).show();

                                            }
                                        });

                                        layoutDelete.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(view.getRootView().getContext());
                                                builder.setTitle("Delete");
                                                builder.setMessage("Are you sure you want to delete this comment ?");
                                                builder.setPositiveButton("YES", (dialogInterface, i) -> {
                                                    //delete comment
                                                    dialog.dismiss();
                                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user_posts");
                                                    ref.child(comment.getUser_id()).child("comments").child(comment.getPost_id()).child(comment.getComment_id()).removeValue();
                                                    ref.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            Toast.makeText(Comment__activity.this, "Deleted", Toast.LENGTH_SHORT).show();
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });


                                                });
                                                builder.setNegativeButton("NO", (dialogInterface, i) -> {
                                                    //dismiss dialog
                                                    dialogInterface.dismiss();
                                                });
                                                //show dialog
                                                builder.create().show();
                                            }
                                        });

                                        dialog.show();
                                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                        dialog.getWindow().setGravity(Gravity.BOTTOM);

                                    }
                                    else{
                                        String message = comment.getComment();
                                        setClipboard(mContext.getApplicationContext(), message);
                                        Toast.makeText(Comment__activity.this, "Copied", Toast.LENGTH_SHORT).show();
                                    }
                                    return true;
                                }
                            });

                        } else {
                            no_comment.setVisibility(View.VISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
    public void  setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);

    }


    private void closeKeyboard(){
        View view = getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private void addNewComment(String newComment){
        Log.d(TAG, "addNewComment: adding new comment: " + newComment);

        String commentID = myRef.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment",newComment);
        hashMap.put("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
        hashMap.put("post_id",video_id);
        hashMap.put("comment_id", commentID);
        hashMap.put("date_created",getTimestamp());

            if(type.equals("0")){
                myRef.child(getString(R.string.dbname_user_texts))
                        .child(user_id)
                        .child(getString(R.string.field_comments))
                        .child(video_id)
                        .child(commentID)
                        .setValue(hashMap);
                myRef.child(getString(R.string.dbname_user_posts))
                        .child(user_id)
                        .child(getString(R.string.field_comments))
                        .child(video_id)
                        .child(commentID)
                        .setValue(hashMap);
            }
            else if(type.equals("1")){
                myRef.child(getString(R.string.dbname_user_photos))
                        .child(user_id)
                        .child(getString(R.string.field_comments))
                        .child(video_id)
                        .child(commentID)
                        .setValue(hashMap);
                myRef.child(getString(R.string.dbname_user_posts))
                        .child(user_id)
                        .child(getString(R.string.field_comments))
                        .child(video_id)
                        .child(commentID)
                        .setValue(hashMap);
            }else if(type.equals("2")){
                myRef.child(getString(R.string.dbname_user_videos))
                        .child(user_id)
                        .child(getString(R.string.field_comments))
                        .child(video_id)
                        .child(commentID)
                        .setValue(hashMap);
            }

    }

    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.FRENCH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+1"));
        return sdf.format(new Date());
    }

    /**
     * retrieve the post from the incoming bundle from profileActivity interface
     * @return
     */

    private void setupWidgets(){
        mCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "It worked", Toast.LENGTH_SHORT).show();

                if(!mComment.getText().toString().equals("")){
                    Log.d(TAG, "onClick: attempting to submit new comment.");
                    addNewComment(mComment.getText().toString());

                    mComment.setText("");
                    closeKeyboard();
                    getComments();
                }else{
                    Toast.makeText(Comment__activity.this, "You can't post a blank comment", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Back", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onClick: navigating back");
                finish();
            }
        });
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();


                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
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