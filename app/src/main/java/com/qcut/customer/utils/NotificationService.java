package com.qcut.customer.utils;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.qcut.customer.R;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NotificationService extends FirebaseMessagingService {

    private NotificationCompat.Builder builder;
    private NotificationManager manager ;
    private int WAITING_NOTIFICATION_ID = 111;
    private String WAITING_NOTIFICATION_CHANNEL_ID = "notify_111";
    private SharedPrefManager sharedPrefManager;

    public NotificationService() {
        super();


    }

    @Override
    public void onNewToken(String token) {
        FireManager.saveNewFirebaseWithGivenToken(token, getApplicationContext());
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData() != null && remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            String currentWaitingTime = data.get("waitingTime");

            String userId = AppUtils.preferences.getString(AppUtils.USER_ID, null);
            boolean isLoggedIn = AppUtils.preferences.getBoolean(AppUtils.IS_LOGGED_IN, false);
            if (isLoggedIn && !StringUtils.isEmpty(userId) && StringUtils.isNumeric(currentWaitingTime)) {
                sharedPrefManager = new SharedPrefManager(getApplicationContext());
                Long currentWaitingTimeReceived = Long.valueOf(currentWaitingTime);
                long lastWaitingTime = sharedPrefManager.getLongSharedPref(SharedPrefManager.LAST_WAITING_TIME);
                boolean showNotification = false;
                String title = "QCut - Waiting Time Update";
                String message = "Your waiting time is "
                        +TimeUtil.getDisplayWaitingTime(currentWaitingTimeReceived)+". ";

                if (lastWaitingTime < 0) {
                    showNotification = true;
                } else if (currentWaitingTimeReceived == 0) {
                    showNotification = true;
                    message = "Your turn has come and barber is waiting for you.";
                } else if (currentWaitingTimeReceived > 0 && currentWaitingTimeReceived <= 15) {
                    if (Math.abs(lastWaitingTime - currentWaitingTimeReceived) >= 3) {
                        showNotification = true;
                        message.concat(" Please arrive the shop as soon as possible.");
                    }
                } else if (currentWaitingTimeReceived > 15) {
                    if (Math.abs(lastWaitingTime - currentWaitingTimeReceived) >= 5) {
                        showNotification = true;
                    }
                }

                if (Math.abs(lastWaitingTime - currentWaitingTimeReceived) >= 5) {
                    showNotification = true;
                }


                //case when time waiting is 0
                //in last 15 min, every 3 min
                //if waiting time is more than 15 min, every 5 min
                //if there is a sudden change of more than 5 min

                if (showNotification) {
                    sharedPrefManager.setLongSharedPref(SharedPrefManager.LAST_WAITING_TIME, currentWaitingTimeReceived);

                    builder =
                            new NotificationCompat.Builder(getApplicationContext(), WAITING_NOTIFICATION_CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_launcher)
                                    .setContentTitle(title)
                                    .setContentText(message);
                    manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    builder.setSound(alarmSound);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        String channelId = WAITING_NOTIFICATION_CHANNEL_ID;
                        NotificationChannel channel = new NotificationChannel(
                                channelId,
                                message,
                                NotificationManager.IMPORTANCE_HIGH);
                        manager.createNotificationChannel(channel);
                        builder.setChannelId(channelId);
                    }

                    manager.notify(WAITING_NOTIFICATION_ID, builder.build());
                }

            }
        }
    }
}
