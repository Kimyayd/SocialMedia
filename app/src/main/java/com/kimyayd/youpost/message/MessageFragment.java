package com.kimyayd.youpost.message;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.kimyayd.youpost.R;
import com.kimyayd.youpost.models.Chat;
import com.kimyayd.youpost.models.UserAccountSettings;
import com.kimyayd.youpost.utils.UserAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageFragment extends Fragment{

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private List<UserAccountSettings> mUsers;
    private DatabaseReference reference;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private TextView textView;
    private FirebaseUser fuser;

    private static final String TAG = "NotificationFragment";
    private Context mContext=getContext();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        setupFirebaseAuth();
        textView =view.findViewById(R.id.messages);
        recyclerView=view.findViewById(R.id.recyclerView_message);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser= FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unread = 0;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.exists()) {
                        Chat chat = dataSnapshot.getValue(Chat.class);
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsseen()) {
                            unread++;
                        }
                        if (unread != 0) {
                            textView.setText("Messages" + "(" + unread + ")");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        chatList();
        return view;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void chatList() {
        mUsers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user_account_settings));
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UserAccountSettings user = dataSnapshot.getValue(UserAccountSettings.class);

                    if (!user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        mUsers.add(user);
                    }
                }
                userAdapter = new UserAdapter(getContext(), mUsers, true);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();

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

    private void status(String status){

        reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user_account_settings)).child(firebaseUser.getUid());
        HashMap<String, Object> hashMap= new HashMap<>();
        hashMap.put("status", status);
        reference.updateChildren(hashMap);
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        mAuth.addAuthStateListener(mAuthListener);
        status("online");
    }

    @Override
    public void onResume(){
        super.onResume();
        status("online");
    }

    @Override
    public void onPause() {
        super.onPause();
        status("offline");
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
            status("offline");
        }
    }


}