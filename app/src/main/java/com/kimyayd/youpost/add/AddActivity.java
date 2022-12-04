package com.kimyayd.youpost.add;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.kimyayd.youpost.R;
import com.kimyayd.youpost.home.SectionPagerAdapter;
import com.kimyayd.youpost.utils.Permissions;

import java.util.ArrayList;

public class AddActivity extends AppCompatActivity {
    private static final String TAG = "AddActivity";
    private static final int ACTIVITY_NUM = 2;
    private Context mContext=AddActivity.this;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;
    private ViewPager2 mViewPager;
    private BottomNavigationView view;
    private ArrayList<Fragment> arr;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        Log.d(TAG, "onCreate: starting.");
        if(checkPermissionsArray(Permissions.PERMISSIONS)){
              setupViewPager();
        }else{
            Toast.makeText(mContext, "Permissions not accorded", Toast.LENGTH_SHORT).show();
            verifyPermissions(Permissions.PERMISSIONS);
        }
    }

    public int getCurrentTabNumber(){
        return mViewPager.getCurrentItem();
    }

    /**
     * setup viewpager for manager the tabs
     */
    private void setupViewPager(){

        arr=new ArrayList<>();
        arr.add(new GalleryFragment());
        arr.add(new PhotoFragment());
        Toast.makeText(mContext, "setupViewPager", Toast.LENGTH_SHORT).show();
        SectionPagerAdapter adapter=new SectionPagerAdapter(this,arr);
        mViewPager=findViewById(R.id.viewpager_container);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabsBottom);
//        new TabLayoutMediator(tabLayout, mViewPager,
//                (tab, position) -> tab.getCustomView()
//        ).attach();
        new TabLayoutMediator(tabLayout, mViewPager, (tab, position) -> {
            if (position==0){
                tab.setText(getString(R.string.gallery));
            }
            else if(position==1) {
                tab.setText(getString(R.string.photo));
            }
        }).attach();

    }


    public int getTask(){
        Log.d(TAG, "getTask: TASK: " + getIntent().getFlags());
        return getIntent().getFlags();
    }

    /**
     * verifiy all the permissions passed to the array
     * @param permissions
     */
    public void verifyPermissions(String[] permissions){
        Log.d(TAG, "verifyPermissions: verifying permissions.");

        ActivityCompat.requestPermissions(
                AddActivity.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );

    }

    /**
     * Check an array of permissions
     * @param permissions
     * @return
     */
    public boolean checkPermissionsArray(String[] permissions){
        Log.d(TAG, "checkPermissionsArray: checking permissions array.");

        for(int i = 0; i< permissions.length; i++){
            String check = permissions[i];
            if(!checkPermissions(check)){
                return false;
            }
        }
        return true;
    }

    public boolean checkPermissions(String permission){
        Log.d(TAG, "checkPermissions: checking permission: " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(AddActivity.this, permission);

        if(permissionRequest != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkPermissions: \n Permission was not granted for: " + permission);
            return false;
        }
        else{
            Log.d(TAG, "checkPermissions: \n Permission was granted for: " + permission);
            return true;
        }
    }

}
