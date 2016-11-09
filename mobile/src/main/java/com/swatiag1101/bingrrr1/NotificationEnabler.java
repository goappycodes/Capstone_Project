package com.swatiag1101.bingrrr1;

import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationDisplayedResult;
import com.onesignal.OSNotificationPayload;

import java.math.BigInteger;

/**
 * Created by Swati Agarwal on 28-07-2016.
 */
public class NotificationEnabler extends NotificationExtenderService {
    @Override
    protected boolean onNotificationProcessing(OSNotificationPayload notification) {
        OverrideSettings overrideSettings = new OverrideSettings();
        overrideSettings.extender = new NotificationCompat.Extender() {
            @Override
            public NotificationCompat.Builder extend(NotificationCompat.Builder builder) {
                // Sets the background notification color to Green on Android 5.0+ devices.
                return builder.setColor(new BigInteger("FF00FF00", 16).intValue());
            }
        };

        OSNotificationDisplayedResult result = displayNotification(overrideSettings);
        Log.d("OneSignalExample", "Notification displayed with id: " + result.notificationId);

        return true;
    }
}
