package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

	private static final String TAG = "PhotoGalleryFragment";

	private RecyclerView mPhotoRecyclerView;
	private List<GalleryItem> mItems = new ArrayList<>();
	private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

	public static PhotoGalleryFragment newInstance() {
		return new PhotoGalleryFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		new FetchItemsTask().execute();

		Handler responseHandler = new Handler();
		mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
		mThumbnailDownloader.setThumbnailDownloadListener(
			new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
				@Override
				public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap) {
					Drawable drawable = new BitmapDrawable(getResources(), bitmap);
					photoHolder.bindDrawable(drawable);
				}
		});
		mThumbnailDownloader.start();
		mThumbnailDownloader.getLooper();
		Log.i(TAG, "Background thread started");

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

		mPhotoRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_photo_gallery_recycler_view);
		mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

		setupAdapter();

		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mThumbnailDownloader.clearQueue();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mThumbnailDownloader.quit();
		Log.i(TAG, "Background thread destroyed");
	}

	private void setupAdapter() {
		if (isAdded()) {
			mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
		}
	}

	private class PhotoHolder extends RecyclerView.ViewHolder {
		private ImageView mImageView;

		public PhotoHolder(View itemView) {
			super(itemView);
			mImageView = (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);
		}

		public void bindDrawable(Drawable drawable) {
			mImageView.setImageDrawable(drawable);
		}
	}

	private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

		private List<GalleryItem> mGalleryItems;

		public PhotoAdapter(List<GalleryItem> galleryItems) {
			mGalleryItems = galleryItems;
		}

		@Override
		public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			View view = inflater.inflate(R.layout.gallery_item, viewGroup, false);
			return new PhotoHolder(view);
		}

		@Override
		public void onBindViewHolder(PhotoHolder photoHolder, int position) {
			GalleryItem galleryItem = mGalleryItems.get(position);
			Bitmap bitmap = mThumbnailDownloader.getCachedImage(galleryItem.getUrl());

			if (bitmap == null) {
				Drawable drawable = getResources().getDrawable(R.drawable.bill_up_close);
				mThumbnailDownloader.queueThumbnail(photoHolder, galleryItem.getUrl());
			} else {
				Log.i(TAG, "Loaded image from cache");
				photoHolder.bindDrawable(new BitmapDrawable(getResources(), bitmap));
			}
			/*Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
			photoHolder.bindDrawable(placeholder);
			mThumbnailDownloader.queueThumbnail(photoHolder, galleryItem.getUrl());*/
		}

		private void preloadAjacentImages(int position) {
			final int imageBufferSize = 10;

			int sIndex = Math.max(position - imageBufferSize, 0);
			int eIndex = Math.min(position + imageBufferSize, mGalleryItems.size() - 1);
			for (int i = sIndex; i < eIndex; i++) {
				if (i == position) {
					continue;
				}
				String url = mGalleryItems.get(i).getUrl();
				mThumbnailDownloader.preloadImage(url);
			}

		}

		@Override
		public int getItemCount() {
			return mGalleryItems.size();
		}
	}

	private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {

		@Override
		protected List<GalleryItem> doInBackground(Void... params) {
			return new FlickrFetchr().fetchItems();
		}

		@Override
		protected void onPostExecute(List<GalleryItem> items) {
			mItems = items;
			setupAdapter();
		}
	}

}
