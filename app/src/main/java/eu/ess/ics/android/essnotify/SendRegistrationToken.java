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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import eu.ess.ics.android.essnotify.datamodel.APN;
import eu.ess.ics.android.essnotify.backend.BackendService;

public class SendRegistrationToken extends AsyncTask<Void, Void, Void> {

    private String firebaseRegistrationToken;
    private Context context;

    public SendRegistrationToken(Context context, String firebaseRegistrationToken){
        this.context = context;
        this.firebaseRegistrationToken = firebaseRegistrationToken;
    }

    @Override
    public Void doInBackground(Void... args) {
        BackendService backendService =
                ServerAPIBase.getInstance().getBackendService(context);
        try {
            APN apn = new APN();
            apn.setApn_token(firebaseRegistrationToken);
            backendService.sendRegistrationToken(apn).execute();
            SharedPreferences sharedPref =
                    context.getSharedPreferences(context.getString(R.string.ess_preferences), Context.MODE_PRIVATE);
            sharedPref.edit().putString(Constants.FIREBASE_REGISTRATION_TOKEN, firebaseRegistrationToken).commit();
        } catch (Exception e) {
           e.printStackTrace();
        }
        return null;
    }
}
