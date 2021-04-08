/*
 * Copyright (C) 2021 European Spallation Source ERIC.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package eu.ess.ics.android.essnotify.pushmessaging;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import eu.ess.ics.android.essnotify.BootstrapActivity;
import eu.ess.ics.android.essnotify.R;

public class PushMessageService extends FirebaseMessagingService {

    public static final String NEW_NOTIFICATION = "NEW_NOTIFICATION";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        sendSingleNotification(remoteMessage.getData().get("title"),
                remoteMessage.getData().get("body"));
    }

    private void sendSingleNotification(String title, String body)
    {
        Intent intent = new Intent(this, BootstrapActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(BootstrapActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_direct_message")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setWhen(System.currentTimeMillis())
                .setContentText(body)
                .setAutoCancel(true)
                .setColor(0xFF004155)
                .setChannelId(getResources().getString(R.string.ess_notification_channel))
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        Notification notification = notificationBuilder.build();

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        notificationManager.notify(1, notification);

        Intent broadcastIntent = new Intent(NEW_NOTIFICATION);
        sendBroadcast(broadcastIntent);
    }
}
