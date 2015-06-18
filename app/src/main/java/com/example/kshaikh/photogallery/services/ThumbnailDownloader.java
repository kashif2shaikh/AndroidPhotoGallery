package com.example.kshaikh.photogallery.services;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kshaikh on 15-06-17.
 */
public class ThumbnailDownloader<Token> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    Map<Token, String> requestMap = Collections.synchronizedMap(new HashMap<Token,String>());

    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    // Use 1/4th of the available memory for this memory cache.
    final int cacheSize = maxMemory / 4;
    private LruCache<String, Bitmap> bitmapCache = new LruCache<String, Bitmap>(cacheSize) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            // The cache size will be measured in kilobytes rather than
            // number of items.
            return bitmap.getByteCount() / 1024;
        }
    };

    Handler mRequestHandler;
    Handler mResponseHandler;
    private Listener<Token> mListener;

    public interface Listener<Token> {
        void onThumbnailDownloaded(Token token, Bitmap thumbnail);
    }

    public void setListener(Listener<Token> listener) {
        mListener = listener;
    }


    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    public void queueThumbnail(final Token token, String url) {
        Log.v(TAG, "Got an URL: " + url);
        requestMap.put(token, url);
        mRequestHandler
                .obtainMessage(MESSAGE_DOWNLOAD, token)
                .sendToTarget();
    }

    @SuppressWarnings("unchecked")
    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {

        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    Token token = (Token)msg.obj;
                    Log.v(TAG, "Got a request for url: " + requestMap.get(token));
                    processRequest(token);
                }
            }
      };
    }

    private void processRequest(final Token token) {
        try {
            final String url = requestMap.get(token);
            if(url == null)
                return;

            Bitmap bitmap = bitmapCache.get(url);
            if(bitmap == null) {
                byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
                bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                bitmapCache.put(url, bitmap);
                Log.v(TAG, "Bitmap created");
            }
            else {
                Log.v(TAG, "Bitmap created from cache");
            }

            final Bitmap finalBitmap = bitmap;
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (requestMap.get(token) != url) {
                        return;
                    }
                    requestMap.remove(token);
                    mListener.onThumbnailDownloaded(token, finalBitmap);
                }
            });
        }catch(IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }
    public void clearQueue() {
        // clear queue so that we remove ImageView objects when fragment's view is destroyed.
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }


}
