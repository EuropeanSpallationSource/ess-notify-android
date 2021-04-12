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

package eu.ess.ics.android.essnotify.backend;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.util.List;

import eu.ess.ics.android.essnotify.LoginActivity;
import eu.ess.ics.android.essnotify.ServerAPIBase;
import eu.ess.ics.android.essnotify.datamodel.UserService;
import eu.ess.ics.android.essnotify.ui.LoginActivityRedirect;
import retrofit2.Call;
import retrofit2.Response;

public class GetSubscriptionsTask extends AsyncTask<Context, Void, List<UserService>> {

    @Override
    public List<UserService> doInBackground(Context... args) {
        BackendService backendService =
                ServerAPIBase.getInstance().getBackendService(args[0]);
        Call<List<UserService>> call = backendService.getUserServices();
        try {
            Response<List<UserService>> response = call.execute();
            if(response.code() == 401){ // ESS token wrong or expired.
                LoginActivityRedirect.goToLogin(args[0]);
                return null;
            }
            return response.body();
        } catch (Exception e) {
            return null;
        }
    }
}
