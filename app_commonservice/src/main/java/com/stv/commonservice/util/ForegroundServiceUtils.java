
package com.stv.commonservice.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class ForegroundServiceUtils {
    public static void setForegroundService(Service service, String channelName, String channelId,
            int smallIcon, String title, String content, String desc,
            boolean autoCancel, boolean onGoing,
            int notificationId) {
        Notification notification;
        if (Build.VERSION.SDK_INT >= 26) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(desc);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(service, channelId);
            builder.setSmallIcon(smallIcon)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(autoCancel)
                    .setOngoing(onGoing);
            NotificationManager notificationManager = service.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notification = builder.build();
        } else {
            notification = new Notification();
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            notification.flags |= Notification.FLAG_NO_CLEAR;
            notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        }
        service.startForeground(notificationId, notification);
    }

}
