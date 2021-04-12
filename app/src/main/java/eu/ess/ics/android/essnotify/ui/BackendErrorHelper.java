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

package eu.ess.ics.android.essnotify.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import eu.ess.ics.android.essnotify.LoginActivity;
import eu.ess.ics.android.essnotify.R;

public class BackendErrorHelper {

    public static void goToLogin(Context context){
        Intent loginIntent = new Intent(context, LoginActivity.class);
        context.startActivity(loginIntent);
    }

    public static void showNetworkErrorDialog(Context context) {

        new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.network_error))
                .setMessage(context.getResources().getString(R.string.network_error_detail))
                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .create()
                .show();
    }
}
