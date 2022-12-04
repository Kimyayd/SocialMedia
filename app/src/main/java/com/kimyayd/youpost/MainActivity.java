package com.kimyayd.youpost;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.elevation.SurfaceColors;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kimyayd.youpost.models.Post;
import com.kimyayd.youpost.models.Text;
import com.kimyayd.youpost.models.Video;
import com.kimyayd.youpost.utils.MainFeedListAdapter;
import com.kimyayd.youpost.utils.UniversalImageLoader;
import com.kimyayd.youpost.utils.ViewCommentsFragment;
import com.kimyayd.youpost.utils.ViewPostCommentsFragment;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity  implements
        MainFeedListAdapter.OnLoadMoreItemsListener{
    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupFirebaseAuth();

        getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_search, R.id.navigation_add, R.id.navigation_messages, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
//         NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

    }


    public void hideSoftKeyboard(){
        if(getCurrentFocus() != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
//    public void hideLayout(){
//        Log.d(TAG, "hideLayout: hiding layout");
//        mRelativeLayout.setVisibility(View.GONE);
//        mFrameLayout.setVisibility(View.VISIBLE);
//    }
//
//
//    public void showLayout(){
//        Log.d(TAG, "hideLayout: showing layout");
//        mRelativeLayout.setVisibility(View.VISIBLE);
//        mFrameLayout.setVisibility(View.GONE);
//    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        if(mFrameLayout.getVisibility() == View.VISIBLE){
//            showLayout();
//        }
//    }

    public void onCommentThreadSelected(Post post, String callingActivity){
        Log.d(TAG, "onCommentThreadSelected: selected a comment thread");

        ViewPostCommentsFragment fragment  = new ViewPostCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.post), post);
        args.putString(getString(R.string.home_activity), getString(R.string.home_activity));
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();

    }
    public void onCommentThreadSelectedVideo(Video video, String callingActivity){
        Log.d(TAG, "onCommentThreadSelectedVideo: selected a comment thread");

        ViewCommentsFragment fragment  = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.video), video);
        args.putString(getString(R.string.home_activity), getString(R.string.home_activity));
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();

    }
    public void onCommentThreadSelectedText(Text text, String callingActivity){
        Log.d(TAG, "onCommentThreadSelectedText: selected a comment thread");

        ViewCommentsFragment fragment  = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.text), text);
        args.putString(getString(R.string.home_activity), getString(R.string.home_activity));
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();

    }

    @Override
    public void onLoadMoreItems() {
//        HomeFragment fragment = (HomeFragment) getFragmentManager().findFragmentById();
//        fragment.displayMorePosts();
    }
    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(MainActivity.this);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();

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
        mAuth.addAuthStateListener(mAuthListener);
        firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        status("online");

    }

    @Override
    protected void onResume(){
        super.onResume();
        status("online");

    }

    @Override
    protected void onPause() {
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