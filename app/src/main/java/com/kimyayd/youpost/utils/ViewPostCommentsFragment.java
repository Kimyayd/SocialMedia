package com.kimyayd.youpost.utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import com.kimyayd.youpost.models.Post;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ViewPostCommentsFragment extends Fragment {

    private static final String TAG = "ViewTextCommFragment";

    public ViewPostCommentsFragment() {
        super();
        setArguments(new Bundle());
    }

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    //widgets
    private ImageView mBackArrow, mCheckMark;
    private EditText mComment;
    private ListView mListView;

    //vars
    private Post mPost;
    private ArrayList<Comment> mComments;
    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_comments, container, false);
        mBackArrow = (ImageView) view.findViewById(R.id.backArrow);
        mCheckMark = (ImageView) view.findViewById(R.id.ivPostComment);
        mComment = (EditText) view.findViewById(R.id.comment);
        mListView = (ListView) view.findViewById(R.id.listView);
        mComments = new ArrayList<>();
        mContext = getActivity();


        try {

            mPost = getPostFromBundle();
            setupFirebaseAuth();

        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }


        return view;
    }

    private void setupWidgets() {

        CommentListAdapter adapter = new CommentListAdapter(mContext,
                R.layout.layout_comment, mComments);
        mListView.setAdapter(adapter);

        mCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "onClicked: checked", Toast.LENGTH_SHORT).show();

                if(!mComment.getText().toString().equals("")){
                    Log.d(TAG, "onClick: attempting to submit new comment.");
                    addNewComment(mComment.getText().toString());

                    mComment.setText("");
                    closeKeyboard();
                }else{
                    Toast.makeText(getActivity(), "you can't post a blank comment", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "onClick: navigating back", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onClick: navigating back");
//                if (getCallingActivityFromBundle().equals(getString(R.string.profile_activity))) {
//                    getActivity().getSupportFragmentManager().popBackStack();
//                    ((ProfileActivity) getActivity()).showLayout();
//                } else {
//                    getActivity().getSupportFragmentManager().popBackStack();
//                }

            }
        });
    }

    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private void addNewComment(String newComment) {
        Log.d(TAG, "addNewComment: adding new comment: " + newComment);

        String commentID = myRef.push().getKey();

        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setDate_created(getTimestamp());
        comment.setComment_id(commentID);
        comment.setPost_id(mPost.getPost_id());
        comment.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        //insert into photos node
        myRef.child(getString(R.string.dbname_texts))
                .child(mPost.getPost_id())
                .child(getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);

        //insert into user_photos node
        myRef.child(getString(R.string.dbname_user_posts))
                .child(mPost.getUser_id()) //should be mphoto.getUser_id()
                .child(mPost.getPost_id())
                .child(getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);

    }

    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        return sdf.format(new Date());
    }

    /**
     * retrieve the photo from the incoming bundle from profileActivity interface
     *
     * @return
     */
    private String getCallingActivityFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getString(getString(R.string.profile_activity));
        } else {
            return null;
        }
    }

    /**
     * retrieve the photo from the incoming bundle from profileActivity interface
     *
     * @return
     */
    private Post getPostFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.post));
        } else {
            return null;
        }
    }

           /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
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

        if (mPost.getComments().size() == 0) {
            mComments.clear();
            Comment firstComment = new Comment();
            firstComment.setComment(mPost.getCaption());
            firstComment.setUser_id(mPost.getUser_id());
            firstComment.setDate_created(mPost.getDate_created());
            mComments.add(firstComment);
            mPost.setComments(mComments);
            setupWidgets();
        }


        myRef.child(mContext.getString(R.string.dbname_posts))
                .child(mPost.getPost_id())
                .child(mContext.getString(R.string.field_comments))
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildAdded: child added.");

                        Query query = myRef
                                .child(mContext.getString(R.string.dbname_posts))
                                .orderByChild(mContext.getString(R.string.field_post_id))
                                .equalTo(mPost.getPost_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                                    Post post = new Post();
                                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                                    post.setCaption(objectMap.get(mContext.getString(R.string.field_caption)).toString());
                                    post.setPost_id(objectMap.get(mContext.getString(R.string.field_post_id)).toString());
                                    post.setUser_id(objectMap.get(mContext.getString(R.string.field_user_id)).toString());
                                    post.setDate_created(objectMap.get(mContext.getString(R.string.field_date_created)).toString());

                                    mComments.clear();
                                    Comment firstComment = new Comment();
                                    firstComment.setComment(mPost.getCaption());
                                    firstComment.setUser_id(mPost.getUser_id());
                                    firstComment.setDate_created(mPost.getDate_created());
                                    mComments.add(firstComment);

                                    for (DataSnapshot dSnapshot : singleSnapshot
                                            .child(mContext.getString(R.string.field_comments)).getChildren()) {
                                        Comment comment = new Comment();
                                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                                        mComments.add(comment);
                                    }

                                    post.setComments(mComments);

                                    mPost = post;

                                    setupWidgets();
                                }

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

