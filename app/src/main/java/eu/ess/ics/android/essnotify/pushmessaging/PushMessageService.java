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

    int id = 1;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        sendNotification(remoteMessage.getData().get("title"),
                remoteMessage.getData().get("body"));
    }

    /**
     * Notifications are grouped, i.e. multiple notifications are presented as one single notification
     * icon, and the notification UI lists all incoming notifications.
     * @param title
     * @param body
     */
    private void sendNotification(String title, String body)
    {
        Intent intent = new Intent(this, BootstrapActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(BootstrapActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        String groupId = "eu.ess.ics.android.essnotify";

        // The below implementation is a result of trial-and-error. The official docs on
        // how to created grouped notifications are (at the time of writing, May 2021)
        // incomplete and consequently confusing.
        NotificationCompat.Builder groupBuilder =
                new NotificationCompat.Builder(this, getResources().getString(R.string.ess_notification_channel))
                        .setContentTitle(title)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentText(body)
                        .setGroupSummary(true)
                        .setGroup(groupId)
                        .setContentIntent(pendingIntent);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, getResources().getString(R.string.ess_notification_channel))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setGroup(groupId)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Yes, this is actually needed! Looks like two separate notifications are dispatched.
        // Note in particular that the first uses a fixed notification id, while the second
        // must increment a notification id. Is this clearly documented by Google? No.
        notificationManager.notify( groupId, 1, groupBuilder.build());
        notificationManager.notify(++id, notificationBuilder.build());

        Intent broadcastIntent = new Intent(NEW_NOTIFICATION);
        sendBroadcast(broadcastIntent);
    }
}
