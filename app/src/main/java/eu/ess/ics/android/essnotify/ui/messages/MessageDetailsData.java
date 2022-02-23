/*
 * Copyright (C) 2022 European Spallation Source ERIC.
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

package eu.ess.ics.android.essnotify.ui.messages;

import android.graphics.drawable.Drawable;

import eu.ess.ics.android.essnotify.datamodel.UserNotification;

public class MessageDetailsData {

    private UserNotification userNotification;
    private String serviceName;
    private Drawable headerColor;

    public MessageDetailsData(UserNotification userNotification, String serviceName, Drawable headerColor){
        this.userNotification = userNotification;
        this.serviceName = serviceName;
        this.headerColor = headerColor;
    }

    public UserNotification getUserNotification() {
        return userNotification;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Drawable getHeaderColor() {
        return headerColor;
    }
}
