package com.kimyayd.youpost.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kimyayd.youpost.MainActivity;
import com.kimyayd.youpost.R;
import com.kimyayd.youpost.models.User;
import com.kimyayd.youpost.profile.ProfileActivity;
import com.kimyayd.youpost.profile.ViewProfilActivity;
import com.kimyayd.youpost.utils.UserListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class SearchFragment extends Fragment {

    private Context mContext = getContext();

    //widgets
    private EditText mSearchParam;
    private ListView mListView;

    //vars
    private List<User> mUserList;
    private UserListAdapter mAdapter;
    private static final String TAG = "SearchFragment";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        Log.d(TAG, "onCreateView: started.");

        mSearchParam = (EditText) view.findViewById(R.id.search);
        mListView = (ListView) view.findViewById(R.id.searchView);
        setupFirebaseAuth();

        readUsers();

        ((MainActivity)getActivity()).hideSoftKeyboard();
        initTextListener();

        return view;
    }
    private void initTextListener(){
        Log.d(TAG, "initTextListener: initializing");

        mUserList = new ArrayList<>();

        mSearchParam.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                searchForMatch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });
    }

    private void searchForMatch(String keyword){
        FirebaseUser fuser=FirebaseAuth.getInstance().getCurrentUser();
            Query query = FirebaseDatabase.getInstance().getReference("users")
                    .orderByChild(getString(R.string.field_username)).startAt(keyword)
                    .endAt(keyword +"\uf0ff");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!mSearchParam.getText().toString().equals("")) {
                        mUserList.clear();
                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                            User user = singleSnapshot.getValue(User.class);
                            assert user != null;
                            assert fuser != null;
                            Log.d(TAG,"User... :" +user.getUser_id());
                            if (!user.getUser_id().equals(fuser.getUid())) {
                                mUserList.add(user);
                            }
                            updateUsersList();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

    }
//    private void searchForMatchs(String keyword){
//        Log.d(TAG, "searchForMatch: searching for a match: " + keyword);
//        mUserList.clear();
//
//        //update the users list view
//        if(keyword.length() ==0){
//
//        }else{
//            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
//            Query query = databaseReference.child(getString(R.string.dbname_users));
//            query.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()) {
//                        if (singleSnapshot.getValue(User.class).getUsername().toLowerCase().contains(keyword.toLowerCase())) {
//                            Log.d(TAG, "onDataChange: found user:" + singleSnapshot.getValue(User.class).toString());
//
//                            mUserList.add(singleSnapshot.getValue(User.class));
//                            //update the users list view
//                            updateUsersList();
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//        }
//    }
    private void updateUsersList(){
        Log.d(TAG, "updateUsersList: updating users list");

        mAdapter = new UserListAdapter(getContext(), R.layout.layout_user_listitem, mUserList);

        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener((parent, view, position, id) -> {
            Log.d(TAG, "onItemClick: selected user: " + mUserList.get(position).toString());

                Intent intent = new Intent(getActivity(), ViewProfilActivity.class);
                intent.putExtra(getString(R.string.intent_user), mUserList.get(position));
                startActivity(intent);
        });
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
    private void readUsers(){
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUserList.clear();
                for(DataSnapshot snapshot1: snapshot.getChildren()){

                    User user=snapshot1.getValue(User.class);
                    assert user!=null;
                    assert firebaseUser!=null;

                    if(!user.getUser_id().equals(firebaseUser.getUid())){

                        mUserList.add(user);

                    }
                }

                updateUsersList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
