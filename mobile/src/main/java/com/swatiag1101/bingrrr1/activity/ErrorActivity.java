package com.swatiag1101.bingrrr1.activity;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.swatiag1101.bingrrr1.MainFragment;
import com.swatiag1101.bingrrr1.R;
import com.swatiag1101.bingrrr1.WebViewAppConfig;

public class ErrorActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        setupActionBar();
        setupDrawer(savedInstanceState);
    }
    private void setupActionBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayUseLogoEnabled(false);
        bar.setDisplayShowTitleEnabled(true);
        bar.setDisplayShowHomeEnabled(true);
        bar.setDisplayHomeAsUpEnabled(WebViewAppConfig.NAVIGATION_DRAWER);
        bar.setHomeButtonEnabled(WebViewAppConfig.NAVIGATION_DRAWER);
        if(!WebViewAppConfig.ACTION_BAR) bar.hide();
    }

    private void setupDrawer(Bundle savedInstanceState)
    {
        // reference
        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_main_drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.activity_main_drawer_navigation);

        // add menu items
        MenuItem firstItem = setupMenu(mNavigationView.getMenu());

        // menu icon tint
        if(!WebViewAppConfig.NAVIGATION_DRAWER_ICON_TINT)
        {
            mNavigationView.setItemIconTintList(null);
        }

        // navigation listener
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                // show interstitial ad
				/*if(WebViewAppConfig.ADMOB_INTERSTITIAL_FREQUENCY > 0 && mInterstitialCounter % WebViewAppConfig.ADMOB_INTERSTITIAL_FREQUENCY == 0)
				{
					showInterstitialAd();
				}
				mInterstitialCounter++;*/

                // select drawer item
                selectDrawerItem(item);
                return true;
            }
        });

        // drawer toggle
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close)
        {
            @Override
            public void onDrawerClosed(View view)
            {
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView)
            {
                supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // disable navigation drawer
        if(!WebViewAppConfig.NAVIGATION_DRAWER)
        {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.LEFT);
        }

        // show initial fragment
        if(savedInstanceState == null)
        {
            if(mUrl == null)
            {
                selectDrawerItem(firstItem);
            }
            else
            {
                selectDrawerItem(mUrl);
            }
        }
    }
    private void selectDrawerItem(MenuItem item)
    {
        int position = item.getItemId();

        String[] urlList = getResources().getStringArray(R.array.navigation_url_list);
        String[] shareList = getResources().getStringArray(R.array.navigation_share_list);

        item.setCheckable(true);
        mNavigationView.setCheckedItem(position);
        getSupportActionBar().setTitle(item.getTitle());
        mDrawerLayout.closeDrawers();
    }

    private void selectDrawerItem(String url)
    {
        mNavigationView.setCheckedItem(-1);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        mDrawerLayout.closeDrawers();
    }

    private MenuItem setupMenu(Menu menu)
    {
        // title list
        String[] titles = getResources().getStringArray(R.array.navigation_title_list);

        // url list
        String[] urls = getResources().getStringArray(R.array.navigation_url_list);

        // icon list
        TypedArray iconTypedArray = getResources().obtainTypedArray(R.array.navigation_icon_list);
        Integer[] icons = new Integer[iconTypedArray.length()];
        for(int i=0; i<iconTypedArray.length(); i++)
        {
            icons[i] = iconTypedArray.getResourceId(i, -1);
        }
        iconTypedArray.recycle();

        // clear menu
        menu.clear();

        // add menu items
        Menu parent = menu;
        MenuItem firstItem = null;
        for(int i = 0; i < titles.length; i++)
        {
            if(urls[i].equals(""))
            {
                // category
                parent = menu.addSubMenu(Menu.NONE, i, i, titles[i]);
            }
            else
            {
                // item
                MenuItem item = parent.add(Menu.NONE, i, i, titles[i]);
                if(icons[i] != -1) item.setIcon(icons[i]);
                if(firstItem == null) firstItem = item;
            }
        }

        return firstItem;
    }

}
