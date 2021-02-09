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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import eu.ess.ics.android.essnotify.datamodel.UserData;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;


public class ServerAPIBase {

	private static ServerAPIBase serverAPI;
	protected ObjectMapper objectMapper;

	public static ServerAPIBase getInstance(){
		if(serverAPI == null){
			serverAPI = new ServerAPIBase();
		}
		return serverAPI;
	}

   public ServerAPIBase() {
	  objectMapper = new ObjectMapper();
	  objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

	public static String getEssToken(Context context) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				context.getString(R.string.ess_preferences), Context.MODE_PRIVATE);
		String userDataSerialized = sharedPref.getString("userData", null);
		try {
			JSONObject jsonObject = new JSONObject(userDataSerialized);
			UserData userData = UserData.fromJsonObject(jsonObject);
			return userData.getFirebaseToken();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public BackendService getBackendService(final Context context, boolean forLogin){

		return getBackendServiceInternal(context, forLogin);
	}

	public BackendService getBackendService(final Context context){

		return getBackendServiceInternal(context, false);
	}

	private BackendService getBackendServiceInternal(final Context context, boolean forLogin){

		OkHttpClient okHttpClient;

		okHttpClient = new OkHttpClient.Builder()
			.connectTimeout(20000, TimeUnit.MILLISECONDS)
			.readTimeout(20000, TimeUnit.MILLISECONDS)
			.addInterceptor(new Interceptor() {
				@Override
				public Response intercept(Chain chain) throws IOException {
					Request.Builder builder = chain.request().newBuilder();
					Request request;
					if(forLogin){
						request = builder.build();
					}
					else{
						request = builder
								.addHeader("Authorization", "Bearer " + getEssToken(context))
								.build();
					}
					return chain.proceed(request);
				}
			}).build();

		ObjectMapper objectMapper =
				new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		Retrofit retrofit = new Retrofit.Builder()
				.client(okHttpClient)
				.baseUrl(Constants.ESS_BACKEND_SERVICE)
				.addConverterFactory(JacksonConverterFactory.create(objectMapper))
				.build();

		return retrofit.create(BackendService.class);
	}
}
