package com.kimyayd.youpost.profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kimyayd.youpost.R;
import com.kimyayd.youpost.models.Post;
import com.kimyayd.youpost.models.User;
import com.kimyayd.youpost.models.UserAccountSettings;
import com.kimyayd.youpost.utils.MainFeedListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextFragment extends Fragment {
    private ArrayList<Post> mPaginatedTexts;
    private ArrayList<String> mFollowing;
    private ArrayList<UserAccountSettings> mUserAccountSettings;
    private List<Post> posts;
    private static final String TAG = "VideoActivity";
    private int resultsCount = 0;
    private RelativeLayout relativeLayout;
    ListView recyclerView;
    MainFeedListAdapter postsAdapter;
    private User mUser;

    public TextFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_text, container, false);

        recyclerView =view.findViewById(R.id.textRecycler);
        recyclerView.setHorizontalFadingEdgeEnabled(true);
        mUser=((ViewProfilActivity)getContext()).getUserFromBundle();



        loadTexts();
        return view;
    }

    private void clearAll(){
        if(mFollowing != null){
            mFollowing.clear();
        }
        if(posts != null){
            posts.clear();
            if(postsAdapter != null){
                postsAdapter.notifyDataSetChanged();
            }
        }
        if(mUserAccountSettings != null){
            mUserAccountSettings.clear();
        }
        if(mPaginatedTexts != null){
            mPaginatedTexts.clear();
        }
        mFollowing = new ArrayList<>();
        posts = new ArrayList<>();
        mPaginatedTexts = new ArrayList<>();
        mUserAccountSettings = new ArrayList<>();
    }

    private void loadTexts() {
        clearAll();
            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbname_user_texts))
                    .child(mUser.getUser_id())
                    .child("text")
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(mUser.getUser_id());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Post post = new Post();
                        Map<String, Object> objectMap = (HashMap<String, Object>) ds.getValue();

                        post.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        post.setPost_id(objectMap.get(getString(R.string.field_post_id)).toString());
                        post.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        post.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        post.setPost_type("0");
//                        List<Comment> commentsList = new ArrayList<Comment>();
//                        for (DataSnapshot dSnapshot : ds
//                                .child(getString(R.string.field_comments)).getChildren()) {
//                            Map<String, Object> object_map = (HashMap<String, Object>) dSnapshot.getValue();
//                            Comment comment = new Comment();
//                            comment.setUser_id(object_map.get(getString(R.string.field_user_id)).toString());
//                            comment.setComment(object_map.get(getString(R.string.field_comment)).toString());
//                            comment.setDate_created(object_map.get(getString(R.string.field_date_created)).toString());
//                            commentsList.add(comment);
//                        }
//                        post.setComments(commentsList);
                        posts.add(post);
                    }

                    postsAdapter = new MainFeedListAdapter(getContext(), R.layout.layout_mainfeed_listitem,posts);
                    recyclerView.setAdapter(postsAdapter);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //in case of error
                    Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

    }

}