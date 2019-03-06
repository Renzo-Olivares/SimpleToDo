package com.renzobiz.simpletodo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class NotificationWorker extends Worker {
    public static final String EXTRA_TITLE = "taskTitle";
    public static final String EXTRA_ID = "taskID";
    private static final String simpletodo_notification_channel = "Task Reminder";
    private UUID taskID;
    private String taskTitle;


    public NotificationWorker(Context context, WorkerParameters params){
        super(context, params);
        taskID = UUID.fromString(getInputData().getString(EXTRA_ID));
        taskTitle = getInputData().getString(EXTRA_TITLE);
    }

    @NonNull
    @Override
    public Result doWork() {
        triggerNotification(taskID, taskTitle);
        return Result.success();
    }

    private void triggerNotification(UUID taskID, String taskTitle) {
        // Create the NotificationChannel on Android O and up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //define the importance level of the notification
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            //build the channel, giving it a unique ID and name
            NotificationChannel channel =
                    new NotificationChannel(simpletodo_notification_channel, simpletodo_notification_channel, importance);

            //add description for channel
            String description = "A channel which shows notifications about tasks at simpletodo";
            channel.setDescription(description);

            //set led color
            channel.setLightColor(Color.MAGENTA);

            //Register channel
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().
                    getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        //create a notification
        //create an intent to open the taskfragment
        Intent intent = TaskPagerActivity.newIntent(getApplicationContext(), taskID, true);

        //put together the PendingIntent
        PendingIntent pendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 1, intent, FLAG_UPDATE_CURRENT);

        //get notification details
        String notificationTitle = "Task Reminder : " + taskTitle;
        String notificationText = "your task is due";

        //build notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), simpletodo_notification_channel)
                        .setSmallIcon(R.drawable.ic_reminder_small)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationText)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //trigger notification
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(getApplicationContext());

        //give notification unique ID
        notificationManager.notify(NotificationID.getID(), notificationBuilder.build());
    }
}
