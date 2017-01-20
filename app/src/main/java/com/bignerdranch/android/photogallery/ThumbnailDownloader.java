package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ThumbnailDownloader<T> extends HandlerThread {
	private static final String TAG = "ThumbnailDownloader";
	private static final int MESSAGE_DOWNLOAD = 0;
	public static final int MESSAGE_PRELOAD = 1;

	private Handler mRequestHandler;
	private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
	private Handler mResponseHandler;
	private ThumbnailDownloadListener<T> mThumbnailDownloadListener;
	private LruCache<String, Bitmap> mLruCache;

	private boolean mHasQuit;

	public ThumbnailDownloader(Handler responseHandler) {
		super(TAG);
		mResponseHandler = responseHandler;
		mLruCache = new LruCache<>(16384);
	}

	public interface ThumbnailDownloadListener<T> {
		void onThumbnailDownloaded(T target, Bitmap thumbnail);
	}

	public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
		mThumbnailDownloadListener = listener;
	}

	@Override
	protected void onLooperPrepared() {
		mRequestHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what){
					case MESSAGE_DOWNLOAD:
						T target = (T) msg.obj;
						Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
						handleRequest(target);
						break;
					case MESSAGE_PRELOAD:
						String url = (String) msg.obj;
						preloadImage(url);
						break;
				}
			}
		};
	}

	@Override
	public boolean quit() {
		mHasQuit = true;
		return super.quit();
	}

	public void queueThumbnail(T target, String url) {
		Log.i(TAG, "Got a URL: " + url);

		if (url == null) {
			mRequestMap.remove(target);
		} else {
			mRequestMap.put(target, url);
			mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
		}
	}

	public void preloadImage(String url) {
		mRequestHandler.obtainMessage(MESSAGE_PRELOAD, url).sendToTarget();
	}

	public void clearQueue() {
		mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
	}

	public void clearCache() {
		mLruCache.evictAll();
	}

	public  Bitmap getCachedImage(String url){
		return mLruCache.get(url);
	}

	private void handleRequest(final T target) {
		final String url = mRequestMap.get(target);
		final Bitmap bitmap;
			if (url == null) {
				return;
			}
		bitmap = downloadImage(url);
		Log.i(TAG, "Bitmap created");
		mResponseHandler.post(new Runnable() {
			@Override
			public void run() {
				if (mRequestMap.get(target) != url || mHasQuit) {
					return;
				}

				mRequestMap.remove(target);
				mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
			}
		});
	}

	private Bitmap downloadImage(final String url) {
		Bitmap bitmap;

		if (url == null) {
			return null;
		}

		bitmap = mLruCache.get(url);
		if (bitmap != null) {
			return bitmap;
		}

		try {
			byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
			bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
			mLruCache.put(url, bitmap);
			Log.i(TAG, "Downloades & cached image: " + url);
			return bitmap;
		} catch (IOException ioe) {
			Log.e(TAG, "Error downloading image", ioe );
			return null;
		}

	}

}
