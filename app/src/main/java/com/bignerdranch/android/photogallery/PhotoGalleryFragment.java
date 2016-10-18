package com.bignerdranch.android.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

	private static final String TAG = "PhotoGalleryFragment";

	private RecyclerView mPhotoRecyclerView;
	private List<GalleryItem> mItems = new ArrayList<>();
	private PhotoAdapter mPhotoAdapter;
	private PhotoHolder mPhotoHolder;
	private int mStatus = 0;
	private int mPosition;

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
		FlickrFetchr.setPage(1);

		mPhotoRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_photo_gallery_recycler_view);
		mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
		mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);

				GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
				Integer lastPosition = manager.findLastVisibleItemPosition();

				if (dy > 0 && lastPosition == (mPosition - 1) && mStatus == 0) {
					new FetchItemsTask().execute();
				}
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
			mPhotoHolder = new PhotoHolder(textView);
			return mPhotoHolder;
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
			mStatus = 1;
			return new FlickrFetchr().fetchItems();

		}

		@Override
		protected void onPostExecute(List<GalleryItem> items) {
			mItems.addAll(mPosition, items);

			mPosition = mItems.size();
			if (mPosition >= 100) {
				mPhotoAdapter.notifyItemRangeInserted(mPosition, 100);
				FlickrFetchr.pageIncrement();
			}
			mStatus = 0;
			setupAdapter();
		}
	}

}
