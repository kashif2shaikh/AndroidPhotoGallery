package com.example.kshaikh.photogallery.common;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.example.kshaikh.photogallery.R;

/**
 * Created by kshaikh on 15-06-16.
 */
public abstract class MasterDetailFragmentActivity extends SingleFragmentActivity {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    protected void setDetailFragment(Fragment newDetailFragment)
    {
        if(!detailFragmentExists())
            return;

        FragmentTransaction ft = mFm.beginTransaction();

        // Remove old fragment if it exists
        Fragment oldDetail = mFm.findFragmentById(R.id.detailFragmentContainer);
        if(oldDetail != null) {
            ft.remove(oldDetail);
        }
        if(newDetailFragment != null)
            ft.add(R.id.detailFragmentContainer, newDetailFragment);
        ft.commit();
    }

    protected void clearDetailFragment()
    {
        if(!detailFragmentExists())
            return;

        FragmentTransaction ft = mFm.beginTransaction();

        // Remove old fragment if it exists
        Fragment oldDetail = mFm.findFragmentById(R.id.detailFragmentContainer);
        if(oldDetail != null) {
            ft.remove(oldDetail);
        }

        ft.commit();
    }

    protected boolean detailFragmentExists() {
        return findViewById(R.id.detailFragmentContainer) != null;
    }

    protected Fragment getDetailFragment()
    {
        return mFm.findFragmentById(R.id.detailFragmentContainer);
    }
}
