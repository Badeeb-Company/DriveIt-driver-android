package com.badeeb.driveit.driver;

/**
 * Created by meldeeb on 9/30/17.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.badeeb.driveit.driver.activity.MainActivity;
import com.badeeb.driveit.driver.controllers.DriveItApplication;

public class ForegroundService extends Service {
    private static final String TAG = ForegroundService.class.getSimpleName();
    public static final String STOP_FOREGROUND_SERVICE = "STOP_FOREGROUND_SERVICE";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "on start command");
        if (intent == null || intent.getBooleanExtra(STOP_FOREGROUND_SERVICE, true)) {
            Log.d(TAG, "stop the service..");
            clearForegroundService();
        } else {
            startForeground(1111, generateNotification("Running"));
            setDriverAvailabilityListener();
        }
        return START_NOT_STICKY;
    }

    private void clearForegroundService() {
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "on destroy");
        clearForegroundService();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification generateNotification(String message) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(DriveItApplication.getInstance());
        notificationBuilder.setAutoCancel(false)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(getString(R.string.app_name))
                .setContentIntent(intent)
                .setContentText(message);

        return notificationBuilder.build();
    }


    public void setDriverAvailabilityListener() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1111, generateNotification("hello"));
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        Log.d(TAG, "on task removed");
        clearForegroundService();
        super.onTaskRemoved(rootIntent);
    }
}