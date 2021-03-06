package com.novoda.downloadmanager.lib;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;

class OkHttpNotificationImageRetriever implements NotificationImageRetriever {

    private final OkHttpClient client;

    private String imageUrl;
    private Bitmap bitmap;

    public OkHttpNotificationImageRetriever() {
        client = new OkHttpClient();
    }

    @Override
    public Bitmap retrieveImage(String imageUrl) {
        if (imageUrl.equals(this.imageUrl)) {
            return bitmap;
        }
        return fetchBitmap(imageUrl);
    }

    private Bitmap fetchBitmap(String imageUrl) {
        Request request = new Request.Builder()
                .get()
                .url(imageUrl)
                .build();
        try {
            Response response = client.newCall(request).execute();
            InputStream inputStream = response.body().byteStream();
            try {
                bitmap = BitmapFactory.decodeStream(inputStream);
                this.imageUrl = imageUrl;
                return bitmap;
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            return null;
        }
    }
}
