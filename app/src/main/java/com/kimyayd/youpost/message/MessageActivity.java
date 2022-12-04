package com.kimyayd.youpost.message;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import com.kimyayd.youpost.utils.MessAdapter;
import com.kimyayd.youpost.utils.UniversalImageLoader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;

    FirebaseUser fuser;
    DatabaseReference reference;
    ImageButton btn_send;
    EditText text_send;
    Intent intent;
    private Context mContext = MessageActivity.this;
    private List<Chat> mChats;
    List<Chat> mchat;
    private String imageurl;
    MessAdapter adapter;
    ListView listView;

    ValueEventListener seenListener;
    String userid;
    boolean notify = false;

    public static final int MSG_TITLE_LEFT=0;
    public static final int MSG_TITLE_RIGHT=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView =findViewById(R.id.listView);
        profile_image=findViewById(R.id.profile_image);
        username=findViewById(R.id.username);
        btn_send=findViewById(R.id.btn_send);
        text_send=findViewById(R.id.text_send);

        intent=getIntent();
        String userid=intent.getStringExtra("userid");
        fuser= FirebaseAuth.getInstance().getCurrentUser();

        btn_send.setOnClickListener(view -> {
            notify = true;
            String msg=text_send.getText().toString();
            if(!msg.equals("")){
                sendMessage(fuser.getUid(),userid,msg);
            }else{
                Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
            }
            text_send.setText("");
        });

        reference= FirebaseDatabase.getInstance().getReference(getString(R.string.user_account_settings)).child(userid);

        reference.addValueEventListener(new ValueEventListener() {
       @Override
       public void onDataChange(@NonNull DataSnapshot snapshot) {
           UserAccountSettings user=snapshot.getValue(UserAccountSettings.class);
           username.setText(user.getUsername());
           if(user.getProfile_photo().equals("default")){
           profile_image.setImageResource(R.drawable.ic_user);}
           else{
               UniversalImageLoader.setImage(user.getProfile_photo(), profile_image, null, "");
           }
           readMessage(fuser.getUid(),userid,user.getProfile_photo());

       }

       @Override
       public void onCancelled(@NonNull DatabaseError error) {

       }
   });
      seenMessage(userid);
    }

    private void seenMessage(String userid){
        reference=FirebaseDatabase.getInstance().getReference("chats");
        seenListener= reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
             for(DataSnapshot dataSnapshot: snapshot.getChildren()){
             Chat chat=dataSnapshot.getValue(Chat.class);
             if(chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)){
                 HashMap<String,Object> hashMap=new HashMap<>();
                 hashMap.put("isseen",true);
                 dataSnapshot.getRef().updateChildren(hashMap);
             }
             }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendMessage(String sender,String receiver, String message) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String chatId = reference.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", chatId);
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);
        hashMap.put("date", getTimestamp());
        hashMap.put("hour", getHourstamp());
        hashMap.put("delete", false);
        reference.child("chats").child(chatId).setValue(hashMap);

    }
    private void readMessage(String myid,String userid,String imageurl){
        mchat=new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference("chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mchat.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Chat chat=dataSnapshot.getValue(Chat.class);
                    assert chat != null;
                    if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        if(!chat.isDelete()) {
                            mchat.add(chat);
                        }

                    }
                    adapter=new MessAdapter(MessageActivity.this,R.layout.chat_item_right,mchat,imageurl);
                    listView.setAdapter(adapter);
                    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Chat chat = mchat.get(i);
                            if (chat.getSender().equals(fuser.getUid())){
                                final Dialog dialog = new Dialog(mContext);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView(R.layout.pop_thing);


                            final Dialog dialogs = new Dialog(mContext);
                            dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialogs.setContentView(R.layout.pop2_thing);
                            dialogs.setTitle("Delete message?");


                            LinearLayout layoutCopy = dialog.findViewById(R.id.layoutCopy);
                            LinearLayout layoutDelete = dialog.findViewById(R.id.layoutDelete);


                            layoutCopy.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                    String message = chat.getMessage();
                                    setClipboard(mContext.getApplicationContext(), message);
                                    Toast.makeText(MessageActivity.this, "Copied", Toast.LENGTH_SHORT).show();

                                }
                            });

                            layoutDelete.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                    if (chat.getSender().equals(fuser.getUid())) {
                                        if (!chat.isIsseen()) {
                                            LinearLayout layoutDeleteEvery = dialogs.findViewById(R.id.layoutDeleteEvery);
                                            LinearLayout layoutDeletes = dialogs.findViewById(R.id.layoutDeletes);
                                            LinearLayout layoutCancel = dialogs.findViewById(R.id.layoutCancel);


                                            layoutDeleteEvery.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    dialogs.dismiss();

                                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                                                    ref.child("chats").child(chat.getId()).removeValue();


                                                }
                                            });
                                            layoutDeletes.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    dialogs.dismiss();

                                                    DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats")
                                                            .child(chat.getId());
                                                    chatRef.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                            chatRef.child("delete").setValue(true);

                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                }
                                            });
                                            layoutCancel.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    dialogs.dismiss();
                                                }
                                            });


                                        } else {
                                            dialogs.dismiss();
                                            DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats")
                                                    .child(chat.getId());
                                            chatRef.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                    chatRef.child("delete").setValue(true);

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                        dialogs.show();
                                        dialogs.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        dialogs.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        dialogs.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                        dialogs.getWindow().setGravity(Gravity.BOTTOM);
                                    }
                                }
                            });

                            dialog.show();
                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                            dialog.getWindow().setGravity(Gravity.BOTTOM);



                        }else{
                                String message = chat.getMessage();
                                setClipboard(mContext.getApplicationContext(), message);
                                Toast.makeText(MessageActivity.this, "Copied", Toast.LENGTH_SHORT).show();
                            }
                            return true;
                    }

                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();

    }

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user_account_settings)).child(fuser.getUid());
        HashMap<String, Object> hashMap= new HashMap<>();
        hashMap.put("status", status);
        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
        currentUser("none");

    }
    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+1"));
        return sdf.format(new Date());
    }
    private String getHourstamp(){
        SimpleDateFormat sdfs =new SimpleDateFormat("HH:mm",Locale.FRENCH);
        sdfs.setTimeZone(TimeZone.getTimeZone("GMT+1"));
        return sdfs.format(new Date());
    }

    public void  setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);

    }

}