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

package eu.ess.ics.android.essnotify.ui.settings;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import eu.ess.ics.android.essnotify.BackendService;
import eu.ess.ics.android.essnotify.R;
import eu.ess.ics.android.essnotify.ServerAPIBase;
import eu.ess.ics.android.essnotify.datamodel.UserService;
import retrofit2.Call;
import retrofit2.Response;

public class SettingsListAdapter extends RecyclerView.Adapter<SettingsListAdapter.ViewHolder>{

    private static final String TAG = "SettingsListAdapter";
    private List<UserService> userServices;
    private List<UserService> filteredUserServices;

    public SettingsListAdapter(List<UserService> userServices){
        this.userServices = userServices;
        this.filteredUserServices = userServices;
    }

    public void filter(String filterText){
        List<UserService> tmp = new ArrayList<>();
        for(UserService userService : userServices){
            if(userService.getCategory().toLowerCase().contains(filterText.toLowerCase())){
                tmp.add(userService);
            }
        }
        filteredUserServices = tmp;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.settings_service_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.d(TAG, "Element " + position + " set.");
        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        viewHolder.getCheckBox().setText(filteredUserServices.get(position).getCategory());
    }

    @Override
    public int getItemCount() {
        return filteredUserServices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final CheckBox checkBox;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Log.d(TAG, "Element " + getAdapterPosition() + " clicked.");
                }
            });
            checkBox = v.findViewById(R.id.userServiceSelected);
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }
    }

    private class HandleSubscriptionTask extends AsyncTask<Void, Void, List<UserService>> {

        private Context context;

        public HandleSubscriptionTask(Context context){
            this.context = context;
        }

        @Override
        public List<UserService> doInBackground(Void... args) {
            BackendService backendService =
                    ServerAPIBase.getInstance().getBackendService(context);
            Call<List<UserService>> call = backendService.getUserServices();
            try {
                Response<List<UserService>> response = call.execute();
                return response.body();
            } catch (Exception e) {
                return null;
            }
        }
    }
}
