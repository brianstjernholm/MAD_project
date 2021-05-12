package dk.au.mad21spring.appproject.gruppe2.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dk.au.mad21spring.appproject.gruppe2.R;
import dk.au.mad21spring.appproject.gruppe2.models.Chat;
import dk.au.mad21spring.appproject.gruppe2.repository.Repository;
import dk.au.mad21spring.appproject.gruppe2.utils.Constants;

//This service is modelled from lecture 05
//https://blackboard.au.dk/webapps/blackboard/content/listContent.jsp?course_id=_145093_1&content_id=_2949310_1&mode=reset
public class NotificationsService extends Service {
    //Variables
    private ExecutorService execService;    //ExecutorService for running things off the main thread
    private boolean started = false;        //Indicating if Service is startet
    private Repository repository;
    private Context context;

    public NotificationsService() { }

    @Override
    public void onCreate() {
        super.onCreate();
        repository = Repository.getInstance(this.getApplication());
        context = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //build notificationchannel
//        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(Constants.SERVICE_CHANNEL, getResources().getString(R.string.notificationservice), NotificationManager.IMPORTANCE_LOW);
//            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            notificationManager.createNotificationChannel(channel);
//        }

        buildNotificationChannel(Constants.SERVICE_CHANNEL, Constants.SERVICE_TAG);
        buildNotificationChannel(Constants.UPDATE_CHANNEL, Constants.UPDATE_TAG);

        Bundle bundle = intent.getExtras();
        String uid = bundle.getString("ThisShouldBeAConstant");

        //Test notification
        Notification testNotification = new NotificationCompat.Builder(this, Constants.SERVICE_CHANNEL)
                .setContentTitle(uid + " just send a message")
                .setSmallIcon(R.mipmap.app_logo_round)
                .build();

//        //Build the notification
//        Notification notification = new NotificationCompat.Builder(this, Constants.SERVICE_CHANNEL)
//                .setContentTitle(getResources().getString(R.string.notificationservice))
//                //.setContentText(getResources().getString(R.string.thisisnotification))
//                .setSmallIcon(R.mipmap.app_logo_round)
//                //.setTicker(getResources().getString(R.string.eachcity))
//                .build();

        //Call to startForeground will promote this Service to a Notification service (manifest permission added)
        //Also require the notification to be set, so that user can always see that Service is running in the background
//        startForeground(Constants.NOTIFICATION_ID, notification);
        startForeground(Constants.NOTIFICATION_ID, testNotification);

        //This method starts recursive background work
        //doBackgroundWork();

        //Returning START_STICKY will make the Service restart again eventually if it gets killed off (e.g. due to resources)
        return START_STICKY;
    }

    //Initatializing service in the background
    private void doBackgroundWork() {
        if(!started) {
            started = true;
            doRecursiveWork();
        }
    }

    private void doRecursiveWork() {
        //Lazy creation of ExecutorService running as a single threaded executor
        //This executor will allow us to do work off the main thread
        if(execService == null) {
            execService = Executors.newSingleThreadExecutor();
        }


    }

    private void buildNotificationChannel(String notificationChannel, String tag) {
        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            NotificationChannel channel =
                    new NotificationChannel(
                            notificationChannel,
                            tag,
                            NotificationManager.IMPORTANCE_LOW);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //If Service is destroyed
    @Override
    public void onDestroy() {
        started = false;
        super.onDestroy();
    }

    //Code adapted from
    //https://stackoverflow.com/questions/25207383/stop-android-service-when-app-is-closed-with-the-task-manager
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        started = false;
        stopSelf();
    }

}
