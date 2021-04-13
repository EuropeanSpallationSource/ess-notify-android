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

package eu.ess.ics.android.essnotify;

import java.text.SimpleDateFormat;

public class Constants {

    /**
     * Name of ESS access token.
     */
    public static final String ESS_TOKEN = "ESS_TOKEN";

    /**
     * Name of Firebase registration token.
     */
    public static final String FIREBASE_REGISTRATION_TOKEN = "FIREBASE_REGISTRATION_TOKEN";

    /**
     * URL for ESS back-end service
     */
    public static final String ESS_BACKEND_SERVICE = "https://notify.esss.lu.se";

    public static final SimpleDateFormat ORIGINAL_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

}
