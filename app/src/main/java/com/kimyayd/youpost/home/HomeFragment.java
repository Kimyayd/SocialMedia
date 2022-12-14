package com.kimyayd.youpost.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kimyayd.youpost.R;
import com.kimyayd.youpost.models.Comment;
import com.kimyayd.youpost.models.Post;
import com.kimyayd.youpost.models.UserAccountSettings;
import com.kimyayd.youpost.video.VideoActivity;
import com.kimyayd.youpost.utils.MainListAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private static final String TAG = "Home";
    private FirebaseAuth firebaseAuth;
    private List<Post> mPosts;

    private ArrayList<Post> mPaginatedPosts;
    private ArrayList<String> mFollowing;
    private int recursionIterator = 0;
    private int resultsCount = 0;
    private LinearLayout mLinearLayout;

    private ArrayList<UserAccountSettings> mUserAccountSettings;
    private ListView mListView;
    private MainListAdapter postsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ImageView chat =view.findViewById(R.id.chat);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().startActivity(new Intent(getContext(), VideoActivity.class));
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        mLinearLayout=view.findViewById(R.id.linearlayout);
        mListView = view.findViewById(R.id.postlists);
        mListView.setHorizontalFadingEdgeEnabled(true);

        mPosts = new ArrayList<>();

        getFollowing();
        return view;
    }

    private void getFriendsAccountSettings(){
        Log.d(TAG, "getFriendsAccountSettings: getting friends account settings.");

        for(int i = 0; i < mFollowing.size(); i++) {
            Log.d(TAG, "getFriendsAccountSettings: user: " + mFollowing.get(i));
            final int count = i;
            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbname_user_account_settings))
                    .orderByKey()
                    .equalTo(mFollowing.get(i));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {


                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Log.d(TAG, "getFriendsAccountSettings: got a user: " + snapshot.getValue(UserAccountSettings.class).getDisplay_name());
                        mUserAccountSettings.add(snapshot.getValue(UserAccountSettings.class));

                        if(count == 0){
                            JSONObject userObject = new JSONObject();
                            try {
                                userObject.put(getString(R.string.field_display_name), mUserAccountSettings.get(count).getDisplay_name());
                                userObject.put(getString(R.string.field_username), mUserAccountSettings.get(count).getUsername());
                                userObject.put(getString(R.string.field_profile_photo), mUserAccountSettings.get(count).getProfile_photo());
                                userObject.put(getString(R.string.field_user_id), mUserAccountSettings.get(count).getUser_id());
                                JSONObject userSettingsStoryObject = new JSONObject();
                                userSettingsStoryObject.put(getString(R.string.user_account_settings), userObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                    }
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void getFollowing() {
        Log.d(TAG, "getFollowing: searching for following");

        clearAll();

        mFollowing.add(FirebaseAuth.getInstance().getCurrentUser().getUid());

        Query query = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                ;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "getFollowing: found user: " + singleSnapshot
                            .child(getString(R.string.field_user_id)).getValue());

                    mFollowing.add(singleSnapshot
                            .child(getString(R.string.field_user_id)).getValue().toString());
                }

                loadPosts();
//                getMyUserAccountSettings();
                getFriendsAccountSettings();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }


    private void clearAll(){
        if(mFollowing != null){
            mFollowing.clear();
        }
        if(mPosts != null){
            mPosts.clear();
            if(postsAdapter != null){
                postsAdapter.notifyDataSetChanged();
            }
        }
        if(mUserAccountSettings != null){
            mUserAccountSettings.clear();
        }
        if(mPaginatedPosts != null){
            mPaginatedPosts.clear();
        }
        mFollowing = new ArrayList<>();
        mPosts = new ArrayList<>();
        mPaginatedPosts = new ArrayList<>();
        mUserAccountSettings = new ArrayList<>();
    }


    private void loadPosts() {

        for (int i = 0; i < mFollowing.size(); i++) {
            final int count = i;
            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getActivity().getString(R.string.dbname_user_posts))
                    .child(mFollowing.get(i))
                    .child("post")
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(mFollowing.get(i));
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "getPhotos: photo: " + snapshot.toString());
                    for (DataSnapshot ds : snapshot.getChildren()) {

                        Post post = new Post();
                        Map<String, Object> objectMap = (HashMap<String, Object>) ds.getValue();

                        post.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        post.setPost_id(objectMap.get(getString(R.string.field_post_id)).toString());
                        post.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        post.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        post.setPost_type(objectMap.get(getString(R.string.field_post_type)).toString());
                        if (!(objectMap.get(getString(R.string.field_post_type)).toString().equals("0")))
                            post.setPost_path(objectMap.get(getString(R.string.field_post_path)).toString());


                        Log.d(TAG, "getPhotos: photo: " + post.getPost_id());
                        List<Comment> commentsList = new ArrayList<Comment>();
                        for (DataSnapshot dSnapshot : ds
                                .child(getString(R.string.field_comments)).getChildren()) {
                            Map<String, Object> object_map = (HashMap<String, Object>) dSnapshot.getValue();
                            Comment comment = new Comment();
                            comment.setUser_id(object_map.get(getString(R.string.field_user_id)).toString());
                            comment.setComment(object_map.get(getString(R.string.field_comment)).toString());
                            comment.setDate_created(object_map.get(getString(R.string.field_date_created)).toString());
                            commentsList.add(comment);
                        }
                        post.setComments(commentsList);
                        mPosts.add(post);


                    }
                    if (count >= mFollowing.size() - 1) {
                        //display the photos
                        displayPosts();



                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //in case of error
                    Toast.makeText(getActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
    private void displayPosts(){
        if(mPosts != null){
            try{
                Collections.sort(mPosts, new Comparator<Post>() {
                    public int compare(Post o1, Post o2) {
                        return o2.getDate_created().compareTo(o1.getDate_created());
                    }
                });

                int iterations = mPosts.size();
                if(iterations > 10){
                    iterations = 10;
                }

                resultsCount = 0;
                for(int i = 0; i < iterations; i++){
                    mPaginatedPosts.add(mPosts.get(i));
                    resultsCount++;
                    Log.d(TAG, "displayPhotos: adding a photo to paginated list: " + mPosts.get(i).getPost_id());
                }
                postsAdapter = new MainListAdapter(getActivity(), R.layout.layout_real,mPosts);

                //set adapter to recyclerview
                mListView.setAdapter(postsAdapter);
            }catch (IndexOutOfBoundsException e){
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException:" + e.getMessage() );
            }catch (NullPointerException e){
                Log.e(TAG, "displayPhotos: NullPointerException:" + e.getMessage() );
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
