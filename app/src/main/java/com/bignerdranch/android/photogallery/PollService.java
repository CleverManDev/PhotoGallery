package com.bignerdranch.android.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.util.Log;

import java.util.List;

public class PollService extends IntentService{
	private static final String TAG ="PollService";

	private static final int POLL_INTERVAL = 1000 * 60; // 60 seconds

	public static Intent newIntent(Context context) {
		return new Intent(context, PollService.class);
	}

	public static void setServiceAlarm(Context context, boolean isOn) {
		Intent i = PollService.newIntent(context);
		PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

		AlarmManager alarmManager = (AlarmManager)
				context.getSystemService(Context.ALARM_SERVICE);
		if (isOn) {
			alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
					SystemClock.elapsedRealtime(), POLL_INTERVAL, pi);
		} else {
			alarmManager.cancel(pi);
			pi.cancel();
		}
	}

	public PollService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (!isNetworkAvailableAndConnected()) {
			return;
		}
//		Log.i(TAG, "Received an intent: " + intent);
		String query = QueryPreferences.getStoredQuery(this);
		String lastResultId = QueryPreferences.getLastResultId(this);
		List<GalleryItem> items;

		if (query == null) {
			items = new FlickrFetchr().fetchRecentPhotos();
		} else {
			items = new FlickrFetchr().searchPhotos(query);
		}
		if (items.size() == 0) {
			return;
		}

		String resultId = items.get(0).getId();
		if (resultId.equals(lastResultId)) {
			Log.i(TAG, "Got an old result: " + resultId);
		} else {
			Log.i(TAG, "Got a new result: " + resultId);
		}
		QueryPreferences.setLastResultId(this, resultId);
	}

	private boolean isNetworkAvailableAndConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		return (cm.getActiveNetworkInfo() != null)
				&& cm.getActiveNetworkInfo().isConnected();
	}
}
