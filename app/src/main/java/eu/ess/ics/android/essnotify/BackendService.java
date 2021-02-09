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

import eu.ess.ics.android.essnotify.datamodel.Login;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * API for calls to the ESS back-end service.
 */
public interface BackendService {

    /**
     * Sends login request to service.
     * @param username User name as entered by user.
     * @param password Password as entered by user.
     * @return Upon successful authentication, a {@link Login} object containing ESS access token.
     * Client code will need to handle failures accordingly.
     */
    @FormUrlEncoded
    @POST("/api/v1/login")
    Call<Login> login(@Field("username") String username, @Field("password") String password);

    /**
     * Registers or refreshes the Firebase token with service.
     * @param firebaseRegistrationToken
     * @return
     */
    @POST("/api/v1/users/user/apn-token")
    Call<Void> sendRegistrationToken(String firebaseRegistrationToken);
}
