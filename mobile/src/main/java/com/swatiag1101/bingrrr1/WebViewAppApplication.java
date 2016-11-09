package com.swatiag1101.bingrrr1;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.onesignal.OneSignal;
import com.swatiag1101.bingrrr1.R;
import com.swatiag1101.bingrrr1.activity.MainActivity;
import com.swatiag1101.bingrrr1.activity.SplashScreen;
import com.swatiag1101.bingrrr1.listener.OneSignalNotificationOpenedHandler;

import org.json.JSONObject;


public class WebViewAppApplication extends Application
{
	private static WebViewAppApplication sInstance;

	private Tracker mTracker;


	public static Context getContext()
	{
		return sInstance;
	}


	public WebViewAppApplication()
	{
		sInstance = this;
	}


	@Override
	public void onCreate()
	{
		super.onCreate();
		MultiDex.install(this);
		// force AsyncTask to be initialized in the main thread due to the bug:
		// http://stackoverflow.com/questions/4280330/onpostexecute-not-being-called-in-asynctask-handler-runtime-exception
		try
		{
			Class.forName("android.os.AsyncTask");
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}

		// initialize OneSignal
		OneSignal.startInit(this)
				.setNotificationOpenedHandler(new BingrrNotificationOpenHandler())
				.setAutoPromptLocation(true)
				.init();
		OneSignal.enableNotificationsWhenActive(true);
		OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
			@Override
			public void idsAvailable(String userId, String registrationId) {
				if (registrationId != null)
					Log.d("debug", "registrationId:" + registrationId);
			}
		});
	}


	public synchronized Tracker getTracker()
	{
		if(mTracker==null)
		{
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			analytics.setDryRun(WebViewAppConfig.ANALYTICS_TRACKING_ID == null || WebViewAppConfig.ANALYTICS_TRACKING_ID.equals(""));
			mTracker = analytics.newTracker(R.xml.analytics_app_tracker);
			mTracker.set("&tid", WebViewAppConfig.ANALYTICS_TRACKING_ID);
		}
		return mTracker;
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}

	private class BingrrNotificationOpenHandler implements OneSignal.NotificationOpenedHandler {
		// This fires when a notification is opened by tapping on it.
		@Override
		public void notificationOpened(String message, JSONObject additionalData, boolean isActive) {
			Intent intent = new Intent(getApplicationContext(), SplashScreen.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}
}
