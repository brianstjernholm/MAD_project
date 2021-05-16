package dk.au.mad21spring.appproject.gruppe2.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.Observer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dk.au.mad21spring.appproject.gruppe2.R;
import dk.au.mad21spring.appproject.gruppe2.activities.MessageActivity;
import dk.au.mad21spring.appproject.gruppe2.models.ChuckNorris;
import dk.au.mad21spring.appproject.gruppe2.models.User;
import dk.au.mad21spring.appproject.gruppe2.repository.Repository;
import dk.au.mad21spring.appproject.gruppe2.utils.Constants;

//This service is adapted from lecture 05
//https://blackboard.au.dk/webapps/blackboard/content/listContent.jsp?course_id=_145093_1&content_id=_2949310_1&mode=reset

public class NotificationsService extends LifecycleService {

    private ExecutorService execService;    //ExecutorService for running things off the main thread
    private Repository repository;
    private Context context;
    private String uid;

    public NotificationsService() { }

    @Override
    public void onCreate() {
        super.onCreate();
        repository = Repository.getInstance(this.getApplication());
        context = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

        buildNotificationChannel(Constants.SERVICE_CHANNEL, Constants.SERVICE_TAG);
        //buildNotificationChannel(Constants.UPDATE_CHANNEL, Constants.UPDATE_TAG);

        uid = new String();

        //Start notification
        Notification startNotification = new NotificationCompat.Builder(this, Constants.SERVICE_CHANNEL)
                .setContentTitle(getResources().getString(R.string.startingNotificationService))
                .setSmallIcon(R.mipmap.app_logo_round)
                .build();

        //Call to startForeground will promote this Service to a Notification service (manifest permission added)
        //Also require the notification to be set, so that user can always see that Service is running in the background
        startForeground(Constants.NOTIFICATION_ID, startNotification);


        repository.observeOnLatestChat().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                uid = s;
                repository.getChuck();
            }
        });

        repository.observeOnChuck().observe(this, new Observer<ChuckNorris>() {
            @Override
            public void onChanged(ChuckNorris chuckNorris) {
                doBackgroundWork(chuckNorris, uid);
            }
        });


        //Returning START_STICKY will make the Service restart again eventually if it gets killed off (e.g. due to resources)
        return START_STICKY;
    }

    //Initializing service in the background
    private void doBackgroundWork(ChuckNorris chuckNorris, String uid) {
        doActualWork(chuckNorris, uid);
    }

    private void doActualWork(ChuckNorris chuckNorris, String uid) {
        //Lazy creation of ExecutorService running as a single threaded executor
        //This executor will allow us to do work off the main thread
        if(execService == null) {
            execService = Executors.newSingleThreadExecutor();
        }

        execService.submit(new Runnable() {
            @Override
            public void run() {
                //Get message sender from repo
                User sender = new User();
                sender = repository.getUserFromDb(uid);

                String input = uid;
                Intent notificationIntent = new Intent(context, MessageActivity.class);
                notificationIntent.putExtra(Constants.USER_ID, input);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

                //Notification manager
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                //Notification
                Notification notification = new NotificationCompat.Builder(context, Constants.SERVICE_CHANNEL)
                        .setSmallIcon(R.drawable.logo_vector)
                        .setContentTitle("Message from " + sender.getUsername())
                        .setContentText(chuckNorris.getValue())
                        .setContentIntent(pendingIntent)
                        .build();

                notificationManager.notify(Constants.NOTIFICATION_ID, notification);
            }
        });

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
        super.onBind(intent);
        return null;
    }

    //If Service is destroyed
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

}
