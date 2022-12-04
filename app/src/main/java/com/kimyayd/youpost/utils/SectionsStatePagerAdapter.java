package com.kimyayd.youpost.utils;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class SectionsStatePagerAdapter extends FragmentStateAdapter {

    private final ArrayList<Fragment> arr= new ArrayList<>();
    private final HashMap<Fragment, Integer> mFragments = new HashMap<>();
    private final HashMap<String, Integer> mFragmentNumbers = new HashMap<>();
    private final HashMap<Integer, String> mFragmentNames = new HashMap<>();
    public SectionsStatePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);

    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return arr.get(position);
    }

    @Override
    public int getItemCount() {
        return arr.size();
    }
    public void addFragment(Fragment fragment,String fragmentName) {
         arr.add(fragment);
        mFragments.put(fragment, arr.size()-1);
        mFragmentNumbers.put(fragmentName, arr.size()-1);
        mFragmentNames.put(arr.size()-1, fragmentName);
    }
    public Integer getFragmentNumber(String fragmentName){
        if(mFragmentNumbers.containsKey(fragmentName)){
            return mFragmentNumbers.get(fragmentName);
        }else{

            return null;
        }
    }
    public Integer getFragmentNumber(Fragment fragment){
        if(mFragmentNumbers.containsKey(fragment)){
            return mFragmentNumbers.get(fragment);
        }else{
            return null;
        }
    }
    public String getFragmentName(Integer fragmentNumber){
        if(mFragmentNames.containsKey(fragmentNumber)){
            return mFragmentNames.get(fragmentNumber);
        }else{
            return null;
        }
    }


}
