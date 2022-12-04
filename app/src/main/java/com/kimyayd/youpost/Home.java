package com.kimyayd.youpost;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kimyayd.youpost.login.LoginActivity;
import com.kimyayd.youpost.models.Post;
import com.kimyayd.youpost.utils.MainFeedListAdapter;

import java.util.ArrayList;
import java.util.List;

public class Home extends Fragment {
    private static final String TAG = "Home";
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<Post> postList;
    Adapter postsAdapter;

    public Home() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        //init
        firebaseAuth = FirebaseAuth.getInstance();
        //recyclerview and it's properties
//        recyclerView = view.findViewById(R.id.postlist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show post first
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);
        //init post list
        postList = new ArrayList<>();
        loadPosts();
        return view;
    }
    private void loadPosts() {
        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("posts");
        //get all data from this ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Post modelPost = ds.getValue(Post.class);
                    postList.add(modelPost);
                    //adapter
                    postsAdapter = new Adapter(getActivity(), postList);
                    //set adapter to recyclerview
                    recyclerView.setAdapter(postsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //in case of error
                Toast.makeText(getActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
//    private void searchPosts(String searchQuery){
//        //path of all posts
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("posts");
//        //get all data from this ref
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                postList.clear();
//                for (DataSnapshot ds : snapshot.getChildren()){
//                    Post modelPost = ds.getValue(Post.class);
//                    if (modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
//                            modelPost.getpDescription().toLowerCase().contains(searchQuery.toLowerCase())){
//                        postList.add(modelPost);
//                    }
//                    //adapter
//                    postsAdapter = new PostsAdapter(getActivity(), postList);
//                    //set adapter to recyclerview
//                    recyclerView.setAdapter(postsAdapter);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                //in case of error
//                showToast("" + error.getMessage());
//            }
//        });
//    }
//private void checkUserStatus(){
//    //get current user
//    FirebaseUser user = firebaseAuth.getCurrentUser();
//    if (user != null){
//        //user is signed in stay here // set email of logged user
//    } else {
//        //user is not signed go to signUp activity
//        startActivity(new Intent(getActivity(), LoginActivity.class));
//        getActivity().finish();
//    }
//}
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
