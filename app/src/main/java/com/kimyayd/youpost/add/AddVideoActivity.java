package com.kimyayd.youpost.add;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kimyayd.youpost.R;
import com.kimyayd.youpost.models.Post;
import com.kimyayd.youpost.models.RealVideo;
import com.kimyayd.youpost.models.Video;
import com.kimyayd.youpost.utils.FilePaths;
import com.kimyayd.youpost.utils.FirebaseMethods;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class AddVideoActivity extends AppCompatActivity {
    private static final String TAG = "Videos";
    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private StorageReference mStorageReference;
    private double mPhotoUploadProgress=0;

    //widgets
    private EditText mCaption;
    private int mFollowersCount = 3;

    //vars
    private String mAppend = "file:/";
    private int videoCount = 1;
    private String videoUrl;
    private Bitmap bitmap;
    private Intent intent;
    private EditText titleEt;
    private VideoView videoView;
    private ImageButton uploadVideoBtn,video_back;
    private FloatingActionButton pickVideoFab;
    private ProgressDialog progressDialog;
    private String title;
    public static final int VIDEO_PICK_GALLERY_CODE = 100;
    public static final int VIDEO_PICK_CAMERA_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;

    private String[] cameraPermissions;
    private Uri videoUri = null;
    private Context mContext=AddVideoActivity.this;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Uploading Video");
        progressDialog.setCanceledOnTouchOutside(true);

        mFirebaseMethods = new FirebaseMethods(mContext);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        titleEt=findViewById(R.id.titleEt);
        videoView=findViewById(R.id.videoView);
        uploadVideoBtn=findViewById(R.id.uploadVideoBtn);
        pickVideoFab=findViewById(R.id.pickVideoFab);
        video_back=findViewById(R.id.video_back);

        cameraPermissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};


        setupFirebaseAuth();

        video_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        uploadVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title=titleEt.getText().toString().trim();
                if(TextUtils.isEmpty(title)){
                    Toast.makeText(mContext,"Title is required...",Toast.LENGTH_SHORT).show();

                }else if(videoUri==null){
                    Toast.makeText(mContext,"Pick a video before you can upload...",Toast.LENGTH_SHORT).show();
                }
                else{
                    uploadNewVideo(title,videoCount,getImageUriFromHere().toString());

                }
            }
        });
        pickVideoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoPickDialog();
            }
        });
    }
    private void videoPickDialog(){
        String[] options={"Camera","Gallery"};

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Pick Video Form")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            if(!checkCameraPermission()){
                                requestCameraPermission();
                            }else{
                                videoPickCamera();
                            }

                        } else if (i == 1) {
                            videoPickGallery();
                        }
                    }
                })
                .show();
    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result1= ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED;
        boolean result2= ContextCompat.checkSelfPermission(this,Manifest.permission.WAKE_LOCK)== PackageManager.PERMISSION_GRANTED;

        return result1 && result2;
    }
    //
//    private void someMethod(){
//        /*
//            Step 1)
//            Create a data model for Photos
//            Step 2)
//            Add properties to the Photo Objects (caption, date, imageUrl, photo_id, tags, user_id)
//            Step 3)
//            Count the number of photos that the user already has.
//            Step 4)
//            a) Upload the photo to Firebase Storage
//            b) insert into 'photos' node
//            c) insert into 'user_photos' node
//         */
//
//    }
//
//
//    /**
//     * gets the image url from the incoming intent and displays the chosen image
//     */
    private void videoPickGallery(){
        Intent intent=new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Videos"),VIDEO_PICK_GALLERY_CODE);

    }
    private void videoPickCamera(){
        Intent intent=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent,VIDEO_PICK_CAMERA_CODE);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case CAMERA_REQUEST_CODE:
                if(grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        videoPickCamera();
                    }else{
                        Toast.makeText(this, "Camera & Storage permission are required", Toast.LENGTH_SHORT).show();
                    }
                }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK){
            if(requestCode==VIDEO_PICK_GALLERY_CODE){
                videoUri=data.getData();
                setVideoToVideoView();
            }
            else if(requestCode==VIDEO_PICK_CAMERA_CODE) {
                videoUri = data.getData();
                setVideoToVideoView();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }
    private Uri getImageUriFromHere(){
        return videoUri;
    }
    private void setVideoToVideoView(){
        MediaController mediaController=new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(videoUri);
        videoView.requestFocus();
        videoView.setOnPreparedListener(mediaPlayer -> videoView.pause());


    }


    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return super.onSupportNavigateUp();
    }

     /*
     ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * Setup the firebase auth object
     */
    public void uploadNewVideo(final String caption, final int count, final String vidUrl
    ) {
        Log.d(TAG, "uploadNewVideo: attempting to uplaod new video.");

        FilePaths filePaths = new FilePaths();
        //case1) new photo
        StorageReference storageReference = mStorageReference.child(filePaths.FIREBASE_VIDEO_STORAGE + "/video" + (count + 1));

        UploadTask uploadTask = null;
        uploadTask = storageReference.putFile(getImageUriFromHere());

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    Uri firebaseUrl = taskSnapshot.getDownloadUrl();
                if (taskSnapshot.getMetadata() != null) {
                    if (taskSnapshot.getMetadata().getReference() != null) {
                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Toast.makeText(mContext, "video upload success", Toast.LENGTH_SHORT).show();
                                addVideoToDatabase(caption,uri.toString());
                                Toast.makeText(mContext, "Here", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Video upload failed.");
                Toast.makeText(mContext, "Video upload failed ", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = 100 + taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                if (progress - 15 > mPhotoUploadProgress) {
                    Toast.makeText(mContext, "Video upload progress", Toast.LENGTH_SHORT).show();
                    mPhotoUploadProgress = progress;
                }
            }
        });


    }
    private void addVideoToDatabase(String caption, String url){
        Log.d(TAG, "addVideoToDatabase: adding video to database.");
        Toast.makeText(mContext, "Here", Toast.LENGTH_SHORT).show();

        String newPostKey = myRef.child(mContext.getString(R.string.dbname_videos)).push().getKey();
//        Video video = new Video();
//        video.setCaption(caption);
//        video.setDate_created(getTimestamp());
//        video.setVideo_path(url);
//        video.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
//     video.setVideo_id(newPostKey);
//        RealVideo realVideo=new RealVideo();
//        realVideo.setVideo(video);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("caption", caption);
        hashMap.put("date_created", getTimestamp());
        hashMap.put("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
        hashMap.put("video_id", newPostKey);
        hashMap.put("video_path", url);
        hashMap.put("post_type", "2");

        assert newPostKey != null;

        myRef.child(mContext.getString(R.string.dbname_user_videos))
                .child(FirebaseAuth.getInstance().getCurrentUser()
                        .getUid()).child("video").child(newPostKey).setValue(hashMap);

        finish();
    }
    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.FRANCE);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+1"));
        return sdf.format(new Date());
    }
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        Log.d(TAG, "onDataChange: image count: " + videoCount);

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

        Toast.makeText(mContext, "Everything is okay!!", Toast.LENGTH_SHORT).show();

        myRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //retrieve user information from the database
                videoCount = mFirebaseMethods.getVideoCount(dataSnapshot);
                Log.d(TAG, "onDataChange: image count: " + videoCount);

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
