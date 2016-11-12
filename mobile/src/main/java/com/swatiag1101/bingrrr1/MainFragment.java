package com.swatiag1101.bingrrr1;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.net.Uri;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.swatiag1101.bingrrr1.activity.ErrorActivity;
import com.swatiag1101.bingrrr1.activity.LoginActivity;
import com.swatiag1101.bingrrr1.data.FoodContract;
import com.swatiag1101.bingrrr1.fragment.TaskFragment;
import com.swatiag1101.bingrrr1.listener.WebViewOnTouchListener;
import com.swatiag1101.bingrrr1.utility.ContentUtility;
import com.swatiag1101.bingrrr1.utility.DownloadUtility;
import com.swatiag1101.bingrrr1.utility.Logcat;
import com.swatiag1101.bingrrr1.utility.NetworkUtility;
import com.swatiag1101.bingrrr1.utility.PermissionUtility;
import com.swatiag1101.bingrrr1.view.StatefulLayout;

import java.io.File;

import name.cpr.VideoEnabledWebChromeClient;
import name.cpr.VideoEnabledWebView;


public class MainFragment extends TaskFragment implements SwipeRefreshLayout.OnRefreshListener, AdvancedWebView.Listener, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener
{
	private static final String ARGUMENT_URL = "url";
	private static final String ARGUMENT_SHARE = "share";
	private static final int REQUEST_FILE_PICKER = 1;
	SharedPreferences preferences;
	protected LocationRequest locationRequest;
	private boolean mActionBarProgress = false;
	private View mRootView;
	String appPackageName = "com.swatiag1101.bingrrr1";
	private StatefulLayout mStatefulLayout;
	private AdvancedWebView mWebView;
	private String mUrl = "about:blank";
	private String mShare;
	private boolean mLocal = false;
	private ValueCallback<Uri> mFilePathCallback4;
	private ValueCallback<Uri[]> mFilePathCallback5;
	Intent i;
	String status;
	String name_filled;
	String password_filled;
	String page;
	MenuItem textOne;
	int notificationId;
	GoogleApiClient googleApiClient;
	protected static final int REQUEST_CHECK_SETTINGS = 0x1;

	public static MainFragment newInstance(String url, String share)
	{
		MainFragment fragment = new MainFragment();

		// arguments
		Bundle arguments = new Bundle();
		arguments.putString(ARGUMENT_URL, url);
		arguments.putString(ARGUMENT_SHARE, share);
		fragment.setArguments(arguments);

		return fragment;
	}


	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);
	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);

		// handle fragment arguments
		Bundle arguments = getArguments();
		if(arguments != null)
		{
			handleArguments(arguments);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){

				WebView.setWebContentsDebuggingEnabled(true);

		}



	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_main, container, false);
		mWebView = (AdvancedWebView) mRootView.findViewById(R.id.fragment_main_webview);
		preferences = getActivity().getSharedPreferences("My_Profile", getActivity().MODE_PRIVATE);
		i = getActivity().getIntent();
		status = i.getStringExtra("Logged");
		name_filled = i.getStringExtra("name");
		password_filled = i.getStringExtra("password");
		page = i.getStringExtra("pages");

		mWebView.setWebViewClient(new MyWebViewClient());
		if (Build.VERSION.SDK_INT >= 19) {
			// chromium, enable hardware acceleration
			mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			// older android version, disable hardware acceleration
			mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		return mRootView;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		// restore webview state
		if(savedInstanceState!=null)
		{
			mWebView.restoreState(savedInstanceState);
		}

		// setup webview
		bindData();

		mWebView.addJavascriptInterface(new WebViewInterface(getContext()), "Android");

		// pull to refresh
		//setupSwipeRefreshLayout();

		// setup stateful layout
		setupStatefulLayout(savedInstanceState);

		// load data
		if(mStatefulLayout.getState()==null) loadData();

		// progress in action bar
		showActionBarProgress(mActionBarProgress);

		// check permissions
		if(WebViewAppConfig.GEOLOCATION)
		{
			//PermissionUtility.checkPermissionAccessLocation(this);
			if(PermissionUtility.checkPermissionAccessLocation(this)) {
				if (googleApiClient == null) {
					googleApiClient = new GoogleApiClient.Builder(getActivity())
							.addApi(LocationServices.API)
							.addConnectionCallbacks(this)
							.addOnConnectionFailedListener(this).build();
					googleApiClient.connect();

					locationRequest =  LocationRequest.create();
					locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
					locationRequest.setInterval(30 * 1000);
					locationRequest.setFastestInterval(5 * 1000);
					LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
							.addLocationRequest(locationRequest);

					//**************************
					builder.setAlwaysShow(true); //this is the key ingredient
					//**************************

					PendingResult<LocationSettingsResult> result =
							LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
					result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
						@Override
						public void onResult(LocationSettingsResult result) {
							final Status status = result.getStatus();
							final LocationSettingsStates state = result.getLocationSettingsStates();
							switch (status.getStatusCode()) {
								case LocationSettingsStatusCodes.SUCCESS:
									// All location settings are satisfied. The client can initialize location
									// requests here.
									break;
								case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
									// Location settings are not satisfied. But could be fixed by showing the user
									// a dialog.
									try {
										// Show the dialog by calling startResolutionForResult(),
										// and check the result in onActivityResult().
										status.startResolutionForResult(
												getActivity(), REQUEST_CHECK_SETTINGS);
									} catch (IntentSender.SendIntentException e) {
										// Ignore the error.
									}
									break;
								case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
									// Location settings are not satisfied. However, we have no way to fix the
									// settings so we won't show the dialog.
									break;
							}
						}
					});
				}
			}
		}
	}


	@Override
	public void onStart()
	{
		super.onStart();
	}


	@Override
	public void onResume() {
		super.onResume();
		mWebView.onResume();
	}


	@Override
	public void onPause()
	{
		super.onPause();
		mWebView.onPause();
	}


	@Override
	public void onStop()
	{
		super.onStop();
	}


	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		mRootView = null;
	}


	@Override
	public void onDestroy()
	{
		super.onDestroy();
		mWebView.onDestroy();
	}


	@Override
	public void onDetach()
	{
		super.onDetach();
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		final LocationSettingsStates states = LocationSettingsStates.fromIntent(intent);
		switch (requestCode) {
			// Check for the integer request code originally supplied to startResolutionForResult().
			case REQUEST_CHECK_SETTINGS:
				switch (resultCode) {
					case Activity.RESULT_OK:
						Log.i("ok", "User agreed to make required location settings changes.");
						//startLocationUpdates();
						break;
					case Activity.RESULT_CANCELED:
						Log.i("canceled", "User chose not to make required location settings changes.");
						break;
				}
				break;
		}
		super.onActivityResult(requestCode, resultCode, intent);
		mWebView.onActivityResult(requestCode, resultCode, intent);
		//handleFilePickerActivityResult(requestCode, resultCode, intent); // not used, used advanced webview instead
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		// save current instance state
		super.onSaveInstanceState(outState);
		setUserVisibleHint(true);

		// stateful layout state
		if(mStatefulLayout!=null) mStatefulLayout.saveInstanceState(outState);

		// save webview state
		mWebView.saveState(outState);
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		// action bar menu
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_main, menu);

		// show or hide share button
		MenuItem share = menu.findItem(R.id.menu_fragment_main_share);
		share.setVisible(mShare != null && !mShare.trim().equals(""));

		MenuItem cart = menu.findItem(R.id.menu_fragment_cart);
		//cart.setVisible(mShare != null && !mShare.trim().equals(""));

		MenuItem account = menu.findItem(R.id.menu_fragment_account);

	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// action bar menu behavior
		switch(item.getItemId())
		{
			case R.id.menu_fragment_main_share:
				startShareActivity(getString(R.string.app_name), getShareText(mShare));
				return true;
			case R.id.menu_fragment_cart:
				mWebView.loadUrl("http://www.bingrrr.in/cart/");
				return true;
			case R.id.menu_fragment_account:
				mWebView.loadUrl("http://www.bingrrr.in/autologin?username=" + name_filled + "&password=" + password_filled + "&rememberme=false&page=my_account");
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		switch(requestCode)
		{
			case PermissionUtility.REQUEST_PERMISSION_READ_EXTERNAL_STORAGE:
			case PermissionUtility.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE:
			case PermissionUtility.REQUEST_PERMISSION_ACCESS_LOCATION:
			{
				// if request is cancelled, the result arrays are empty
				if(grantResults.length > 0)
				{
					for(int i=0; i<grantResults.length; i++)
					{
						if(grantResults[i] == PackageManager.PERMISSION_GRANTED)
						{

						}
						else
						{
							// permission denied
						}
					}
				}
				else
				{
					// all permissions denied
				}
				break;
			}
		}
	}


	@Override
	public void onRefresh()
	{
		runTaskCallback(new Runnable() {
			@Override
			public void run() {
				refreshData();
			}
		});
	}


	@Override
	public void onPageStarted(String url, Bitmap favicon)
	{
		Logcat.d("");
	}


	@Override
	public void onPageFinished(String url)
	{
		Logcat.d("");
	}


	@Override
	public void onPageError(int errorCode, String description, String failingUrl)
	{
		Logcat.d("");
	}


	@Override
	public void onDownloadRequested(String url, String userAgent, String contentDisposition, String mimetype, long contentLength)
	{
		Logcat.d("");
	}


	@Override
	public void onExternalPageRequest(String url)
	{
		Logcat.d("");
	}


	private void handleArguments(Bundle arguments)
	{
		if(arguments.containsKey(ARGUMENT_URL))
		{
			mUrl = arguments.getString(ARGUMENT_URL);
			mLocal = mUrl.contains("file://");
		}
		if(arguments.containsKey(ARGUMENT_SHARE))
		{
			mShare = arguments.getString(ARGUMENT_SHARE);
		}
	}


	// not used, used advanced webview instead
	private void handleFilePickerActivityResult(int requestCode, int resultCode, Intent intent)
	{
		if(requestCode==REQUEST_FILE_PICKER)
		{
			if(mFilePathCallback4!=null)
			{
				Uri result = intent==null || resultCode!=Activity.RESULT_OK ? null : intent.getData();
				if(result!=null)
				{
					String path = ContentUtility.getPath(getActivity(), result);
					Uri uri = Uri.fromFile(new File(path));
					mFilePathCallback4.onReceiveValue(uri);
				}
				else
				{
					mFilePathCallback4.onReceiveValue(null);
				}
			}

			if(mFilePathCallback5!=null)
			{
				Uri result = intent==null || resultCode!=Activity.RESULT_OK ? null : intent.getData();
				if(result!=null)
				{
					String path = ContentUtility.getPath(getActivity(), result);
					Uri uri = Uri.fromFile(new File(path));
					mFilePathCallback5.onReceiveValue(new Uri[]{ uri });
				}
				else
				{
					mFilePathCallback5.onReceiveValue(null);
				}
			}

			mFilePathCallback4 = null;
			mFilePathCallback5 = null;
		}
	}


	private void loadData()
	{
		if(NetworkUtility.isOnline(getActivity()) || mLocal)
		{
			// show progress
			mStatefulLayout.showProgress();

			// load web url
			mWebView.loadUrl(mUrl);
		}
		else
		{
			mStatefulLayout.showOffline();
		}
	}


	public void refreshData()
	{
		if(NetworkUtility.isOnline(getActivity()) || mLocal)
		{
			// show progress in action bar
			showActionBarProgress(true);

			// load web url
			String url = mWebView.getUrl();
			if(url == null || url.equals("")) url = mUrl;
			mWebView.loadUrl(url);
		}
		else
		{
			showActionBarProgress(false);
			Toast.makeText(getActivity(), R.string.global_offline_toast, Toast.LENGTH_LONG).show();
		}
	}


	private void showActionBarProgress(boolean visible)
	{
		// show pull to refresh progress bar
	/*	SwipeRefreshLayout contentSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.container_content_swipeable);
		SwipeRefreshLayout offlineSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.container_offline_swipeable);
		SwipeRefreshLayout emptySwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.container_empty_swipeable);

		contentSwipeRefreshLayout.setRefreshing(visible);
		offlineSwipeRefreshLayout.setRefreshing(visible);
		emptySwipeRefreshLayout.setRefreshing(visible);

		boolean enabled;
		if(WebViewAppConfig.PULL_TO_REFRESH) enabled = !visible;
		else enabled = false;

		contentSwipeRefreshLayout.setEnabled(enabled);
		offlineSwipeRefreshLayout.setEnabled(enabled);
		emptySwipeRefreshLayout.setEnabled(enabled);

		mActionBarProgress = visible;*/
	}


	private void showContent(final long delay)
	{
		final Handler timerHandler = new Handler();
		final Runnable timerRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				runTaskCallback(new Runnable()
				{
					public void run()
					{
						if(getActivity()!=null && mRootView!=null)
						{
							Logcat.d("timer");
							mStatefulLayout.showContent();
						}
					}
				});
			}
		};
		timerHandler.postDelayed(timerRunnable, delay);
	}


	private void bindData()
	{
		// webview settings
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setAppCacheEnabled(true);
		mWebView.getSettings().setAppCachePath(getActivity().getCacheDir().getAbsolutePath());
		mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
		mWebView.getSettings().setDomStorageEnabled(true);
		mWebView.getSettings().setDatabaseEnabled(true);
		mWebView.getSettings().setGeolocationEnabled(true);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.getSettings().setBuiltInZoomControls(false);
		// advanced webview settings
		mWebView.setListener(getActivity(), this);
		mWebView.setGeolocationEnabled(true);

		// webview style
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY); // fixes scrollbar on Froyo

		// webview hardware acceleration
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
		{
			mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		}
		else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}

		// webview chrome client
		View nonVideoLayout = getActivity().findViewById(R.id.activity_main_non_video_layout);
		ViewGroup videoLayout = (ViewGroup) getActivity().findViewById(R.id.activity_main_video_layout);
		View progressView = getActivity().getLayoutInflater().inflate(R.layout.placeholder_progress, null);
		VideoEnabledWebChromeClient webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, progressView, (VideoEnabledWebView) mWebView);
		webChromeClient.setOnToggledFullscreen(new MyToggledFullscreenCallback());
		mWebView.setWebChromeClient(webChromeClient);
		//mWebView.setWebChromeClient(new MyWebChromeClient()); // not used, used advanced webview instead

		// webview client
		mWebView.setWebViewClient(new MyWebViewClient());

		// webview key listener
		//mWebView.setOnKeyListener(new WebViewOnKeyListener());
		mWebView.setOnKeyListener(new View.OnKeyListener()
		{
			private int mCloseCounter = 0;

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{

				if(event.getAction() == KeyEvent.ACTION_DOWN)
				{
					WebView webView = (WebView) v;

					switch(keyCode)
					{
						case KeyEvent.KEYCODE_BACK:
							if (webView.canGoBack()) {
									webView.goBack();
									return true;
							}else{
								mCloseCounter++;
							}
							if(mCloseCounter<2){
								Toast.makeText(getContext(),"Please press back again to exit",Toast.LENGTH_LONG).show();
							}else{
								Intent intent = new Intent(getActivity(), LoginActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								intent.putExtra("EXIT", true);
								startActivity(intent);
							}
							break;
					}
				}
				if (keyCode == KeyEvent.KEYCODE_MENU) {
					// your action...
					DrawerLayout mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.activity_main_drawer_layout);
					if (!mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
						mDrawerLayout.openDrawer(Gravity.LEFT);
					}
					return true;
				}
				return false;
			}
		});
		// webview touch listener
		mWebView.requestFocus(View.FOCUS_DOWN); // http://android24hours.blogspot.cz/2011/12/android-soft-keyboard-not-showing-on.html
		mWebView.setOnTouchListener(new WebViewOnTouchListener());

		// webview scroll listener
		//((RoboWebView) mWebView).setOnScrollListener(new WebViewOnScrollListener()); // not used

		// admob
		bindDataBanner();
	}


	private void bindDataBanner()
	{
		if(WebViewAppConfig.ADMOB_UNIT_ID_BANNER != null && !WebViewAppConfig.ADMOB_UNIT_ID_BANNER.equals("") && NetworkUtility.isOnline(getActivity()))
		{
			// reference
			ViewGroup contentLayout = (ViewGroup) mRootView.findViewById(R.id.container_content);

			// layout params
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER;

			// create ad view
			AdView adView = new AdView(getActivity());
			adView.setId(R.id.adview);
			adView.setLayoutParams(params);
			adView.setAdSize(AdSize.SMART_BANNER);
			adView.setAdUnitId(WebViewAppConfig.ADMOB_UNIT_ID_BANNER);

			// add to layout
			//contentLayout.removeView(getActivity().findViewById(R.id.adview));
			//contentLayout.addView(adView);

			// call ad request
			AdRequest adRequest = new AdRequest.Builder()
					.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
					.addTestDevice(WebViewAppConfig.ADMOB_TEST_DEVICE_ID)
					.build();
			adView.loadAd(adRequest);
		}
	}


	private void controlBack()
	{
		if(mWebView.canGoBack())
				mWebView.goBack();

	}


	private void controlForward()
	{
		if(mWebView.canGoForward()) mWebView.goForward();
	}


	private void controlStop()
	{
		mWebView.stopLoading();
	}


	private void controlReload()
	{
		mWebView.reload();
	}


	private void setupStatefulLayout(Bundle savedInstanceState)
	{
		// reference
		mStatefulLayout = (StatefulLayout) mRootView;

		// state change listener
		mStatefulLayout.setOnStateChangeListener(new StatefulLayout.OnStateChangeListener()
		{
			@Override
			public void onStateChange(View v, StatefulLayout.State state)
			{
				Logcat.d("" + (state == null ? "null" : state.toString()));
				// do nothing
			}
		});

		// restore state
		mStatefulLayout.restoreInstanceState(savedInstanceState);
	}


	private void setupSwipeRefreshLayout()
	{
		/*SwipeRefreshLayout contentSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.container_content_swipeable);
		SwipeRefreshLayout offlineSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.container_offline_swipeable);
		SwipeRefreshLayout emptySwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.container_empty_swipeable);

		if(WebViewAppConfig.PULL_TO_REFRESH)
		{
			contentSwipeRefreshLayout.setOnRefreshListener(this);
			offlineSwipeRefreshLayout.setOnRefreshListener(this);
			emptySwipeRefreshLayout.setOnRefreshListener(this);
		}
		else
		{
			contentSwipeRefreshLayout.setEnabled(false);
			offlineSwipeRefreshLayout.setEnabled(false);
			emptySwipeRefreshLayout.setEnabled(false);
		}*/
	}


	private void startWebActivity(String url)
	{
		try
		{
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
		}
		catch(ActivityNotFoundException e)
		{
			// can't start activity
		}
	}


	private void startEmailActivity(String email, String subject, String text)
	{
		try
		{
			StringBuilder builder = new StringBuilder();
			builder.append("mailto:");
			builder.append(email);

			Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(builder.toString()));
			intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
			intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
			startActivity(intent);
		}
		catch(ActivityNotFoundException e)
		{
			// can't start activity
		}
	}


	private void startCallActivity(String url)
	{
		try
		{
			Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
			startActivity(intent);
		}
		catch(ActivityNotFoundException e)
		{
			// can't start activity
		}
	}


	private void startSmsActivity(String url)
	{
		try
		{
			Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
			startActivity(intent);
		}
		catch(ActivityNotFoundException e)
		{
			// can't start activity
		}
	}


	private void startMapSearchActivity(String url)
	{
		try
		{
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
		}
		catch(android.content.ActivityNotFoundException e)
		{
			// can't start activity
		}
	}


	private void startShareActivity(String subject, String text)
	{
		try
		{
			Intent intent = new Intent(android.content.Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
			intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
			startActivity(intent);
		}
		catch(android.content.ActivityNotFoundException e)
		{
			// can't start activity
		}
	}


	private String getShareText(String text)
	{
		if(mWebView != null)
		{
			/* if(mWebView.getTitle() != null)
			{
				text = text.replaceAll("\\{TITLE\\}", mWebView.getTitle());
			}
			if(mWebView.getUrl() != null)
			{
				text = text.replaceAll("\\{URL\\}", mWebView.getUrl());
			}*/

			text = "market://details?id=" + appPackageName;
		}
		return text;
	}


	private boolean isLinkExternal(String url)
	{
		for(String rule : WebViewAppConfig.LINKS_OPENED_IN_EXTERNAL_BROWSER)
		{
			if(url.contains(rule)) return true;
		}
		return false;
	}


	private boolean isLinkInternal(String url)
	{
		for(String rule : WebViewAppConfig.LINKS_OPENED_IN_INTERNAL_WEBVIEW)
		{
			if(url.contains(rule)) return true;
		}
		return false;
	}

	@Override
	public void onConnected(Bundle bundle) {
		//Log.v("Inside onConnected",bundle.toString());
	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.v("Inside onConnection",i+"");
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.v("Inside onConneF",connectionResult.toString());
	}


	// not used, used advanced webview instead
	private class MyWebChromeClient extends WebChromeClient
	{
		public void openFileChooser(ValueCallback<Uri> filePathCallback)
		{
			if(PermissionUtility.checkPermissionReadExternalStorage(MainFragment.this))
			{
				mFilePathCallback4 = filePathCallback;
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("*/*");
				startActivityForResult(Intent.createChooser(intent, "File Chooser"), REQUEST_FILE_PICKER);
			}
		}


		public void openFileChooser(ValueCallback filePathCallback, String acceptType)
		{
			if(PermissionUtility.checkPermissionReadExternalStorage(MainFragment.this))
			{
				mFilePathCallback4 = filePathCallback;
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("*/*");
				startActivityForResult(Intent.createChooser(intent, "File Chooser"), REQUEST_FILE_PICKER);
			}
		}




		public void openFileChooser(ValueCallback<Uri> filePathCallback, String acceptType, String capture)
		{
			if(PermissionUtility.checkPermissionReadExternalStorage(MainFragment.this))
			{
				mFilePathCallback4 = filePathCallback;
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("*/*");
				startActivityForResult(Intent.createChooser(intent, "File Chooser"), REQUEST_FILE_PICKER);
			}
		}


		@Override
		public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
		{
			if(PermissionUtility.checkPermissionReadExternalStorage(MainFragment.this))
			{
				mFilePathCallback5 = filePathCallback;
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("*/*");
				startActivityForResult(Intent.createChooser(intent, "File Chooser"), REQUEST_FILE_PICKER);
				return true;
			}
			return false;
		}


		@Override
		public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback)
		{
			callback.invoke(origin, true, false);
		}
	}


	private class MyToggledFullscreenCallback implements VideoEnabledWebChromeClient.ToggledFullscreenCallback
	{
		@Override
		public void toggledFullscreen(boolean fullscreen)
		{
			if(fullscreen)
			{
				WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
				attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
				attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
				getActivity().getWindow().setAttributes(attrs);
				if(android.os.Build.VERSION.SDK_INT >= 14)
				{
					getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
				}
			}
			else
			{
				WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
				attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
				attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
				getActivity().getWindow().setAttributes(attrs);
				if(android.os.Build.VERSION.SDK_INT >= 14)
				{
					getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
				}
			}
		}
	}


	private class MyWebViewClient extends WebViewClient
	{
		private boolean mSuccess = true;
		private boolean flag=false;

		SharedPreferences preferences1 = getActivity().getSharedPreferences("My_Profile",Context.MODE_PRIVATE);

		@Override
		public void onPageFinished(final WebView view, final String url)
		{
			mRootView.findViewById(R.id.loading_screen).setVisibility(View.GONE);
			//show webview
			mRootView.findViewById(R.id.fragment_main_webview).setVisibility(View.VISIBLE);
			runTaskCallback(new Runnable() {
				public void run() {
					if (getActivity() != null && mSuccess) {
						//hide loading image

						showContent(500); // hide progress bar with delay to show webview content smoothly
						showActionBarProgress(false);
					}
				}
			});

		}


		@SuppressWarnings("deprecation")
		@Override
		public void onReceivedError(final WebView view, final int errorCode, final String description, final String failingUrl)
		{
			if(errorCode!=0){
				//Intent i = new Intent(getActivity(), ErrorActivity.class);
				//startActivity(i);
				mWebView.loadUrl("file:///android_asset/error.html");
			} else {
				runTaskCallback(new Runnable() {
					public void run() {
						if (getActivity() != null) {
							mSuccess = false;
							//mStatefulLayout.showEmpty();
							mWebView.loadUrl("http://www.bingrrr.in/m-restaurant-page/");
							showActionBarProgress(false);
						}
					}
				});
			}
			//Toast.makeText(getActivity(),"error",Toast.LENGTH_SHORT).show();

		}


		@TargetApi(Build.VERSION_CODES.M)
		@Override
		public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
		{
			// forward to deprecated method
			onReceivedError(view, error.getErrorCode(), error.getDescription().toString(), request.getUrl().toString());
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			if ("http://www.bingrrr.in/auto-logout/".equals(url) || "http://bingrrr.in/auto-logout/".equals(url)){
				SharedPreferences.Editor editor = preferences1.edit();
				editor.putString("Status", "Logged out");
				editor.apply();

				ContentValues contentValues = new ContentValues();
				contentValues.put(FoodContract.FoodEntry.status, "Logged out");

				getActivity().getContentResolver().update(FoodContract.FoodEntry.CONTENT_URI,contentValues,null,null);

				Intent i = new Intent(getActivity(),LoginActivity.class);
				i.putExtra("Logged", "No");
				i.putExtra("name", preferences1.getString("Name",""));
				i.putExtra("password", preferences1.getString("Password",""));
				startActivity(i);

			}

			if ("http://www.bingrrr.in/tomyaccount/".equals(url) || "http://bingrrr.in/tomyaccount/".equals(url)) {
				mWebView.loadUrl("http://www.bingrrr.in/autologin?username=" + name_filled + "&password=" + password_filled + "&rememberme=false&page=my_account");
			}

			if("http://www.bingrrr.in/error-my-account".equals(url) || "http://bingrrr.in/error-my-account".equals(url)) {

				ContentValues contentValues = new ContentValues();
				contentValues.put(FoodContract.FoodEntry.status, "Wrong Details");

				getActivity().getContentResolver().delete(FoodContract.FoodEntry.CONTENT_URI,null,null);

				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("Name", "");
				editor.putString("Password", "");
				editor.putString("Status", "Wrong Details");
				editor.apply();

				Intent i = new Intent(getActivity(),LoginActivity.class);
				i.putExtra("name", preferences.getString("Name", ""));
				i.putExtra("password", preferences.getString("Password", ""));
				startActivity(i);
			}
			String lati = preferences.getString("Latitude","");
			String longi = preferences.getString("Longitude","");

			if ("http://www.bingrrr.in/".equals(url) || "http://bingrrr.in/".equals(url)) {

				if(status.equals("Yes") && !name_filled.isEmpty() && !password_filled.isEmpty()){
					mWebView.loadUrl("http://www.bingrrr.in/autologin?username=" + name_filled + "&password=" + password_filled + "&rememberme=true&page=Login");
				}else if(status.equals("No") && !name_filled.isEmpty() && !password_filled.isEmpty()) {
					Intent intent = new Intent(getActivity(), LoginActivity.class);
					startActivity(intent);
				}else{
					mWebView.loadUrl("http://www.bingrrr.in/m/");
				}
			}

			if(url.contains("http://www.bingrrr.in/checkout/") || url.contains("http://bingrrr.in/checkout/")){
				if(!flag){
					mWebView.loadUrl(url+"?lat="+lati+"&longi="+longi);
					flag=true;
				}

			}
			super.onPageStarted(view, url, favicon);

			mRootView.findViewById(R.id.loading_screen).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.imageView3).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			//show webview
			mRootView.findViewById(R.id.fragment_main_webview).setVisibility(View.GONE);
			//controlBack();
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
			if(DownloadUtility.isDownloadableFile(url))
			{
				if(PermissionUtility.checkPermissionWriteExternalStorage(MainFragment.this))
				{
					Toast.makeText(getActivity(), R.string.fragment_main_downloading, Toast.LENGTH_LONG).show();
					DownloadUtility.downloadFile(getActivity(), url, DownloadUtility.getFileName(url));
					return true;
				}
				return true;
			}
			else if(url != null && (url.startsWith("http://") || url.startsWith("https://")))
			{
				// determine for opening the link externally or internally
				boolean external = isLinkExternal(url);
				boolean internal = isLinkInternal(url);
				if(!external && !internal)
				{
					external = WebViewAppConfig.OPEN_LINKS_IN_EXTERNAL_BROWSER;
				}

				// open the link
				if(external)
				{
					startWebActivity(url);
					return true;
				}
				else
				{
					showActionBarProgress(true);
					return false;
				}
			}
			else if(url != null && url.startsWith("mailto:"))
			{
				MailTo mailTo = MailTo.parse(url);
				startEmailActivity(mailTo.getTo(), mailTo.getSubject(), mailTo.getBody());
				return true;
			}
			else if(url != null && url.startsWith("tel:"))
			{
				startCallActivity(url);
				return true;
			}
			else if(url != null && url.startsWith("sms:"))
			{
				startSmsActivity(url);
				return true;
			}
			else if(url != null && url.startsWith("geo:"))
			{
				startMapSearchActivity(url);
				return true;
			}
			else
			{
				return false;
			}
		}
	}

	private class WebViewInterface {

		Context mContext;

		WebViewInterface(Context c){
			mContext = c;
		}

		@JavascriptInterface
		public void showToast(String toast) {
			String a[] = toast.split(",");
			SharedPreferences.Editor e = preferences.edit();
			e.putString("Latitude",a[0]);
			e.putString("Longitude",a[1]);
			e.putString("Offers",a[2]);
			e.commit();
		}
	}

}
