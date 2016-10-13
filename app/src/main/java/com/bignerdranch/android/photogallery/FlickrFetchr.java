package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickrFetchr {

	private static final String TAG = "FlickrFetchr";

	private static final String API_KEY = "9a19232716c2b4dd2edf26277474c926";

	public byte[] getUrlBytes(String urlSpec) throws IOException {
		URL url = new URL(urlSpec);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = connection.getInputStream();
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new IOException(connection.getResponseMessage() + ": width " + urlSpec);
			}

			int bytesRead = 0;
			byte[] buffer = new byte[1024];
			while ((bytesRead = in.read(buffer)) > 0) {
				out.write(buffer, 0, bytesRead);
			}
			out.close();
			return out.toByteArray();
		} finally {
			connection.disconnect();
		}
	}

	public String getUrlString(String urlSpec) throws IOException {
		return new String(getUrlBytes(urlSpec));
	}

	public List<GalleryItem> fetchItems() {

		List<GalleryItem> items = new ArrayList<>();

		try {
			String url = Uri.parse("https://api.flickr.com/services/rest/")
				.buildUpon()
				.appendQueryParameter("method", "flickr.photos.getRecent")
				.appendQueryParameter("api_key", API_KEY)
				.appendQueryParameter("format", "json")
				.appendQueryParameter("nojsoncallback", "1")
				.appendQueryParameter("extras", "url_s")
				.build().toString();
			String jsonString = getUrlString(url);
			Log.i(TAG, "Received JSON: " + jsonString);

			JSONObject jsonBody = new JSONObject(jsonString);

			parseItems(items, jsonBody);
		} catch (JSONException je) {
			Log.e(TAG, "Failed to parse JSON", je);
		} catch (IOException ioe) {
			Log.e(TAG, "Failed to fetch items", ioe);
		}
		return items;
	}

	private void parseItems(List<GalleryItem> items, JSONObject jsonBody)
			throws IOException, JSONException {
		JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
		JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");
		Gson gson = new Gson();

		for (int i = 0; i < photoJsonArray.length(); i++) {
//			JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
			String photoJsonObject = gson.toJson(photoJsonArray.getJSONObject(i));



			GalleryItem item = gson.;

			/*item.setId(photoJsonObject.getString("id"));
			item.setTitle(photoJsonObject.getString("title"));

			if (!photoJsonObject.has("url_s")) {
				continue;
			}
			item.setUrl_s(photoJsonObject.getString("url_s"));*/
			//item.setTitle(photoJsonObject);
			items.add(item);

		}
	}
}
