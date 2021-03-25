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

package eu.ess.ics.android.essnotify.ui.messages;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.ess.ics.android.essnotify.BR;
import eu.ess.ics.android.essnotify.BackendService;
import eu.ess.ics.android.essnotify.R;
import eu.ess.ics.android.essnotify.ServerAPIBase;
import eu.ess.ics.android.essnotify.databinding.ServiceItemBinding;
import eu.ess.ics.android.essnotify.databinding.UserNotificationItemBinding;
import eu.ess.ics.android.essnotify.datamodel.Service;
import eu.ess.ics.android.essnotify.datamodel.UserNotification;
import eu.ess.ics.android.essnotify.datamodel.UserService;
import eu.ess.ics.android.essnotify.ui.settings.ServiceItemClickListener;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Adapter for items in the service settings list.
 */
public class MessagesListAdapter extends RecyclerView.Adapter<MessagesListAdapter.ViewHolder> {

    private List<UserNotification> userNotifications;
    private Context context;

    public MessagesListAdapter() {
        // Instantiate with an empty list to avoid NPEs.
        this.userNotifications = new ArrayList<>();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        context = recyclerView.getContext();
        new GetMessagesTask().execute();
    }

    public void setServicesList(List<UserNotification> servicesList) {
        this.userNotifications = servicesList;
        notifyDataSetChanged();
    }

    @Override
    public MessagesListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        UserNotificationItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(viewGroup.getContext()),
                R.layout.user_notification_item, viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        UserNotification dataModel = userNotifications.get(position);
        viewHolder.bind(dataModel);
        //viewHolder.binding.setItemClickListener(this);
    }

    @Override
    public int getItemCount() {
        return userNotifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private UserNotificationItemBinding binding;

        public ViewHolder(UserNotificationItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Object obj) {
            binding.setVariable(BR.notification, obj);
            binding.executePendingBindings();
        }
    }

    private class GetMessagesTask extends AsyncTask<Void, Void, List<UserNotification>> {

        @Override
        public List<UserNotification> doInBackground(Void... args) {
            BackendService backendService =
                    ServerAPIBase.getInstance().getBackendService(context);
            Call<List<UserNotification>> call = backendService.getNotifications();
            try {
                Response<List<UserNotification>> response = call.execute();
                return response.body();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public void onPostExecute(List<UserNotification> userServiceList) {
            if (userServiceList != null) {
                userServiceList.sort((u1, u2) -> u2.getTimestamp().compareTo(u1.getTimestamp()));
                setServicesList(userServiceList);
            }
            else{
                // TODO: if list cannot be retrieved, UI should show some error message.
            }
        }
    }
}