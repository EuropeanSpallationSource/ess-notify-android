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

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;

import eu.ess.ics.android.essnotify.datamodel.Login;
import eu.ess.ics.android.essnotify.backend.BackendService;
import retrofit2.Call;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.loginButton);

        getSupportActionBar().hide();
    }

    public void login(View view){
        runOnUiThread(() -> loginButton.setEnabled(false));
        try {
            Login login = new LoginTask(((TextInputEditText)findViewById(R.id.username)).getText().toString(),
                    ((TextInputEditText)findViewById(R.id.password)).getText().toString())
                        .execute().get();
            if(login != null){
                SharedPreferences sharedPref =
                        getSharedPreferences(getString(R.string.ess_preferences), Context.MODE_PRIVATE);
                sharedPref.edit().putString(Constants.ESS_TOKEN, login.getAccess_token()).commit();
                Intent vIntent = new Intent(this, MainActivity.class);
                startActivity(vIntent);
                finish();
            }
            else{
                runOnUiThread(() -> loginButton.setEnabled(true));
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.login_failed))
                        .setMessage(getString(R.string.invalid_credentials))
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        } catch (Exception e) {
            runOnUiThread(() -> loginButton.setEnabled(true));
            e.printStackTrace();
        }
    }

    private class LoginTask extends AsyncTask<Void, Void, Login> {

        private String userName;
        private String password;

        public LoginTask(String userName, String password){
            this.userName = userName.trim();
            this.password = password.trim();
        }

        @Override
        public Login doInBackground(Void... args) {
            BackendService backendService =
                    ServerAPIBase.getInstance().getBackendService(LoginActivity.this, true);
            Call<Login> call = backendService.login(userName, password);
            try {
                return call.execute().body();
            } catch (Exception e) {
                return null;
            }
        }
    }
}