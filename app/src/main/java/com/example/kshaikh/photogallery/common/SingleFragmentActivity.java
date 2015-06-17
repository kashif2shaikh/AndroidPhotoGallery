package com.example.kshaikh.photogallery.common;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.example.kshaikh.photogallery.R;

/**
 * Created by kshaikh on 15-06-04.
 */
public abstract class SingleFragmentActivity extends AppCompatActivity {

    protected abstract Fragment createFragment();

    protected FragmentManager mFm;

    protected int getLayoutResId() {
        return R.layout.single_fragment_activity;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        mFm = getSupportFragmentManager();
        addSingleFragment();
    }



    private void addSingleFragment()
    {
        Fragment fragment = mFm.findFragmentById(R.id.fragmentContainer);
        if(fragment == null) {
            // This looks confusing, but when activity is re-created on rotation for instance, the fragments
            // are not destroyed
            fragment = createFragment();
            mFm.beginTransaction()
                    .add(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }

    protected Fragment getFragment()
    {
        return mFm.findFragmentById(R.id.fragmentContainer);
    }
}
