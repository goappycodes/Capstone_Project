/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.swatiag1101.bingrrr1.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.util.Log;
import com.swatiag1101.bingrrr1.R;
import com.swatiag1101.bingrrr1.activity.SplashScreen;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * IntentService which handles updating all Today widgets with the latest data
 */
public class TodayWidgetIntentService extends IntentService {

    public TodayWidgetIntentService() {
        super("TodayWidgetIntentService");
    }
    SharedPreferences preferences;
    String widget_val;
    String pattern = "HH:mm";
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                TodayWidgetProvider.class));

        String string2 = "12:00:00";
        String time2 = null;
        try {
            time2 = sdf.format(sdf.parse(string2));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String someRandomTime = "21:30:00";
        String d = null;
        try {
            d = sdf.format(sdf.parse(someRandomTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar c = Calendar.getInstance();
        String current_time = sdf.format(c.getTime());

        if (current_time.compareTo(time2)>0 && current_time.compareTo(someRandomTime)<0){
            widget_val = "Store is Open now. Click to Binge!";
        }else{
            widget_val = "Store is Closed now. Open from noon to 9 PM";
        }

        if (widget_val == null) {
            return;
        }

        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget;

            RemoteViews views = new RemoteViews(getPackageName(), layoutId);
            views.setTextViewText(R.id.widget_offer, widget_val);

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, SplashScreen.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
