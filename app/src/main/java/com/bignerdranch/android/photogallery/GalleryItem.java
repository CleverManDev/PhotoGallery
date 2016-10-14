package com.bignerdranch.android.photogallery;

import com.google.gson.annotations.SerializedName;

public class GalleryItem {

	@SerializedName("id")
	private String mId;
	@SerializedName("title")
	private String mCaption;
	@SerializedName("url_s")
	private String mUrl_s;

	@Override
	public String toString() {
		return mCaption;
	}

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		mId = id;
	}

	public String getCaption() {
		return mCaption;
	}

	public void setCaption(String caption) {
		mCaption = caption;
	}

	public String getUrl_s() {
		return mUrl_s;
	}

	public void setUrl_s(String url_s) {
		mUrl_s = url_s;
	}
}
