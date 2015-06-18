package com.example.kshaikh.photogallery.services;

import android.net.Uri;
import android.util.Log;

import com.example.kshaikh.photogallery.models.GalleryItem;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by kshaikh on 15-06-17.
 */
public class FlickrFetchr {
    public static final String TAG = "FlickrFetchr";

    public static final String PREF_SEARCH_QUERY = "searchQuery";
    public static final String PREF_LAST_RESULT_ID = "lastResultId";


    private static final String ENDPOINT = "https://api.flickr.com/services/rest";
    private static final String API_KEY = "f8715392b7d3f143cfff363cbfce7aa9";

    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String METHOD_SEARCH = "flickr.photos.search";

    private static final String PARAM_EXTRAS = "extras";
    private static final String EXTRA_SMALL_URL = "url_s";

    private static final String PARAM_FORMAT = "format";
    private static final String FORMAT_JSON = "json";

    private static final String PARAM_NOJSON_CALLBACK = "nojsoncallback";
    private static final String PARAM_TEXT = "text";

    private static final String JSON_PHOTO_OBJECT = "photos";
    private static final String JSON_PHOTO_ARRAY = "photo";


    public ArrayList<GalleryItem> downloadGalleryItems(String url) {
        try {
            String dataString = getUrl(url);
            Log.i(TAG, "Received flickr data: " + dataString);
            return parsePhotoItems(dataString);
        }catch(IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }
        return null;
    }

    public ArrayList<GalleryItem> fetchItems() {
        Log.i(TAG, "Fetching items");
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", METHOD_GET_RECENT)
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                .appendQueryParameter(PARAM_FORMAT, FORMAT_JSON)
                .appendQueryParameter(PARAM_NOJSON_CALLBACK, "1")
                .build().toString();

        return downloadGalleryItems(url);
    }

    public ArrayList<GalleryItem> search(String query) {
        Log.i(TAG, "Search items with query: " + query);
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", METHOD_SEARCH)
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter(PARAM_TEXT, query)
                .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                .appendQueryParameter(PARAM_FORMAT, FORMAT_JSON)
                .appendQueryParameter(PARAM_NOJSON_CALLBACK, "1")
                .build().toString();

        return downloadGalleryItems(url);
    }

    ArrayList<GalleryItem> parsePhotoItems(String dataString) {
        JsonParser gsonParser = new JsonParser();

        // Strucuture is like { photos: { photo:[p1,p2,p3,etc]} }
        JsonObject rootObject = gsonParser.parse(dataString).getAsJsonObject();
        JsonObject photosObject = rootObject.getAsJsonObject(JSON_PHOTO_OBJECT);
        JsonArray photoArray = photosObject.getAsJsonArray(JSON_PHOTO_ARRAY);

        Gson gson = new Gson();
        Type collectionType = new TypeToken<ArrayList<GalleryItem>>(){}.getType();
        ArrayList<GalleryItem> galleryItems = gson.fromJson(photoArray, collectionType);

        return galleryItems;
    }

    byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();

        }finally {
            connection.disconnect();
        }
    }
    public String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }
}
