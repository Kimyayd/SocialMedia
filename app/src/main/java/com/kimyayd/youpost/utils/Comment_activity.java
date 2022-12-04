package com.kimyayd.youpost.utils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kimyayd.youpost.R;
import com.kimyayd.youpost.models.Comment;
import com.kimyayd.youpost.models.RealVideo;
import com.kimyayd.youpost.models.Video;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class Comment_activity extends AppCompatActivity {
    private static final String TAG = "Comment_activity";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    //widgets
    private ImageView mBackArrow, mCheckMark;
    private EditText mComment;
    private ListView mListView;

    //vars
    private Video video;
    private RealVideo realVideo;
    private ArrayList<Comment> mComments;
    private ArrayList<Comment> comments;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        mBackArrow = (ImageView) findViewById(R.id.backArrow);
        mCheckMark = (ImageView) findViewById(R.id.ivPostComment);
        mComment = (EditText) findViewById(R.id.comment);
        mListView = (ListView) findViewById(R.id.listView);
        mComments = new ArrayList<>();
        mContext = Comment_activity.this;
        comments=new ArrayList<>();



         setupFirebaseAuth();

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
        hashMap.put("post_id",video.getVideo_id());
        hashMap.put("comment_id", commentID);
        hashMap.put("date_created",getTimestamp());

        //insert into photos node
        myRef.child(getString(R.string.dbname_videos))
                .child(getString(R.string.field_comments))
                .child(video.getVideo_id())
                .child(commentID)
                .setValue(hashMap);

        //insert into user_photos node
        myRef.child(getString(R.string.dbname_user_videos))
                .child(video.getUser_id())
                .child(getString(R.string.field_comments))
                .child(video.getVideo_id())
                .child(commentID)
                .setValue(hashMap);

    }

    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.FRANCE);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+1"));
        return sdf.format(new Date());
    }

    /**
     * retrieve the photo from the incoming bundle from profileActivity interface
     * @return
     */

    private void setupWidgets(){
     Log.d(TAG,"Yass: "+mComments);
        CommentListAdapter adapter = new CommentListAdapter(mContext,
                R.layout.layout_comment, mComments);
        mListView.setAdapter(adapter);

        mCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "It worked", Toast.LENGTH_SHORT).show();

                if(!mComment.getText().toString().equals("")){
                    Log.d(TAG, "onClick: attempting to submit new comment.");
                    addNewComment(mComment.getText().toString());

                    mComment.setText("");
                    closeKeyboard();
                }else{
                    Toast.makeText(Comment_activity.this, "You can't post a blank comment", Toast.LENGTH_SHORT).show();
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
        Log.d(TAG,"The moment real is: "+  ("kimyayd"));
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();


                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        realVideo=new RealVideo();
        ArrayList<String> lists;
        lists =getIntent().getStringArrayListExtra("videoItem");
        Video video=new Video();
        video.setCaption(lists.get(0));
        video.setDate_created(lists.get(1));
        video.setVideo_path(lists.get(2));
        video.setVideo_id(lists.get(3));
        video.setUser_id(lists.get(4));
        realVideo.setVideo(video);

        DatabaseReference references =FirebaseDatabase.getInstance().getReference("videos");
        references.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot1 :snapshot.child("comments").child(video.getVideo_id()).getChildren()){
                    Log.d(TAG,"Commentsss "+dataSnapshot1);

                    if(dataSnapshot1.exists()){

                        mComments.add(dataSnapshot1.getValue(Comment.class));

                    }

                }

                if(mComments.size()!=0) {

                    realVideo.setComments(mComments);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(realVideo.getComments().size()==0){
            mComments.clear();
            Comment firstComment = new Comment();
            firstComment.setComment(video.getCaption());
            firstComment.setUser_id(video.getUser_id());
            firstComment.setDate_created(video.getDate_created());
            mComments.add(firstComment);
            realVideo.setComments(mComments);
            setupWidgets();
        }
        myRef.child(mContext.getString(R.string.dbname_videos))
                .child(mContext.getString(R.string.field_comments))
                .child(video.getVideo_id())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildAdded: child added.");

                        Query query = myRef
                                .child(mContext.getString(R.string.dbname_videos))
                                .child("video")
                                .orderByChild(mContext.getString(R.string.field_video_id))
                                .equalTo(video.getVideo_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){

                                    Video videos = new Video();
                                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                                    videos.setCaption(objectMap.get(mContext.getString(R.string.field_caption)).toString());
                                    videos.setVideo_id(objectMap.get(mContext.getString(R.string.field_video_id)).toString());
                                    videos.setUser_id(objectMap.get(mContext.getString(R.string.field_user_id)).toString());
                                    videos.setDate_created(objectMap.get(mContext.getString(R.string.field_date_created)).toString());
                                    videos.setVideo_path(objectMap.get(mContext.getString(R.string.field_video_path)).toString());


                                    mComments.clear();
                                    Comment firstComment = new Comment();
                                    firstComment.setComment(video.getCaption());
                                    firstComment.setUser_id(video.getUser_id());
                                    firstComment.setDate_created(video.getDate_created());
                                    mComments.add(firstComment);

                              DatabaseReference my=FirebaseDatabase.getInstance().getReference("videos");
                              my.addValueEventListener(new ValueEventListener() {
                                  @Override
                                  public void onDataChange(@NonNull DataSnapshot snapshots) {

                                      for (DataSnapshot dSnapshot : snapshots.child("comments").child(video.getVideo_id()).getChildren()){
                                          Log.d(TAG,"mCommentsss "+dSnapshot);
                                          Comment comment = new Comment();
                                          comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                          comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                          comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                                          mComments.add(comment);
                                      }

                                  }

                                  @Override
                                  public void onCancelled(@NonNull DatabaseError error) {

                                  }
                              });
                                    RealVideo realVideo1=new RealVideo();
                                    realVideo1.setComments(mComments);



                                }
                                setupWidgets();



                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(TAG, "onCancelled: query cancelled.");
                            }
                        });
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

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