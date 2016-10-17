package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

	private static final String TAG = "PhotoGalleryFragment";

	private RecyclerView mPhotoRecyclerView;
	private List<GalleryItem> mItems = new ArrayList<>();
	private PhotoAdapter mPhotoAdapter;

	public static PhotoGalleryFragment newInstance() {
		return new PhotoGalleryFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		new FetchItemsTask().execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

		mPhotoRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_photo_gallery_recycler_view);
		mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
		mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);

				GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
				Integer last_page = manager.findLastVisibleItemPosition();

				if ((dy > 0) & (last_page == 99)) {
					Toast.makeText(getContext(), last_page.toString() + " " + Integer.toString(dy), Toast.LENGTH_SHORT).show();

				}

//				Context context = getActivity();
//				String msg = Integer.toString(manager.findLastVisibleItemPosition());
//				Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
			}
		});
		setupAdapter();
		return view;
	}

	private void setupAdapter() {
		if (isAdded()) {
			mPhotoAdapter = new PhotoAdapter(mItems);
			mPhotoRecyclerView.setAdapter(mPhotoAdapter);
		}
	}

	private class PhotoHolder extends RecyclerView.ViewHolder {
		private TextView mTitleTextView;

		public PhotoHolder(View itemView) {
			super(itemView);
			mTitleTextView = (TextView) itemView;
		}

		public void bindGalleryItem(GalleryItem item, int position) {
			mTitleTextView.setText(Integer.toString(position + 1) + " " + item.toString());
		}
	}

	private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

		private List<GalleryItem> mGalleryItems;

		public PhotoAdapter(List<GalleryItem> galleryItems) {
			mGalleryItems = galleryItems;
		}

		@Override
		public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
			TextView textView = new TextView(getActivity());
			return new PhotoHolder(textView);
		}

		@Override
		public void onBindViewHolder(PhotoHolder holder, int position) {
			GalleryItem galleryItem = mGalleryItems.get(position);
			holder.bindGalleryItem(galleryItem, position);
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
			mItems.addAll(items);
			setupAdapter();
		}
	}

}
