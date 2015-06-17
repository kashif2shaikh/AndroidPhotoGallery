package com.example.kshaikh.photogallery;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.kshaikh.photogallery.common.SingleFragmentActivity;


public class PhotoGalleryActivity extends SingleFragmentActivity {


    @Override
    protected Fragment createFragment() {
        return new PhotoGalleryFragment();
    }
}
