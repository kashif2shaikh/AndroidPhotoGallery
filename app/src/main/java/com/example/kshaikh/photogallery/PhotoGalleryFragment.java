package com.example.kshaikh.photogallery;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;

import com.example.kshaikh.photogallery.models.GalleryItem;
import com.example.kshaikh.photogallery.services.FlickrFetchr;
import com.example.kshaikh.photogallery.services.ThumbnailDownloader;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by kshaikh on 15-06-17.
 */
public class PhotoGalleryFragment extends Fragment {

    public static final String PREF_SEARCH_QUERY = "searchQuery";

    private static final String TAG = "PhotoGalleryFragment";
    private GridView mGridView;
    private ArrayList<GalleryItem> mGalleryItems;
    private ThumbnailDownloader<ImageView> mThumbnailThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        mThumbnailThread = new ThumbnailDownloader<ImageView>(new Handler());
        mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
            @Override
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
                if(isVisible()) {
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });
        mThumbnailThread.start();
        mThumbnailThread.getLooper();
        Log.i(TAG, "Background thread started");

        updateItems();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.photo_gallery_fragment, container, false);

        mGridView = (GridView)v.findViewById(R.id.gridView);

        setupAdapter();
        return v;
    }

    void setupAdapter() {
        if(getActivity() == null || mGridView == null) return;

        if(mGalleryItems != null) {
            mGridView.setAdapter(new GalleryItemAdapter(mGalleryItems));
        }
        else {
            mGridView.setAdapter(null);
        }
    }

    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem> {
        public GalleryItemAdapter(ArrayList<GalleryItem> items) {
            super(getActivity(), 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.gallery_item, parent, false);
            }

            ImageView imageView = (ImageView)convertView.findViewById(R.id.gallery_item_imageView);
            imageView.setImageResource(R.drawable.brian_up_close);
            GalleryItem item = getItem(position);
            mThumbnailThread.queueThumbnail(imageView, item.getUrl());
            return convertView;
        }
    }

    public void updateItems() {
        new FetchItemsTask().execute();
    }

    private class FetchItemsTask extends AsyncTask<Void,Void,ArrayList<GalleryItem>> {
        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... voids) {
            Activity activity = getActivity();
            if (activity == null)
                return new ArrayList<GalleryItem>();

            //String query = "Google Nexus";  // TESTING
            String query = PreferenceManager.getDefaultSharedPreferences(activity).getString(PREF_SEARCH_QUERY, null);
            if(query != null) {
                return new FlickrFetchr().search(query);
            }
            else {
                return new FlickrFetchr().fetchItems();
            }
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
            setupAdapter();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailThread.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailThread.clearQueue();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.photo_gallery_fragment, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        SearchView searchView = (SearchView)searchItem.getActionView();

        SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
        ComponentName name = getActivity().getComponentName();
        SearchableInfo searchInfo = searchManager.getSearchableInfo(name);

        searchView.setSearchableInfo(searchInfo);
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Log.i(TAG, "Closing search");
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(PhotoGalleryFragment.PREF_SEARCH_QUERY, null)
                        .commit();
                updateItems();
                return false;
            }
        });
        //searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_search:
                getActivity().onSearchRequested(); // legacy
                return true;
            case R.id.menu_item_refresh:
//                PreferenceManager.getDefaultSharedPreferences(getActivity())
//                        .edit()
//                        .putString(PhotoGalleryFragment.PREF_SEARCH_QUERY, null)
//                        .commit();
                updateItems(); // refresh
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
