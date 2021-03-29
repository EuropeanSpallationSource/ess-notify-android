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
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import eu.ess.ics.android.essnotify.BR;
import eu.ess.ics.android.essnotify.backend.BackendService;
import eu.ess.ics.android.essnotify.R;
import eu.ess.ics.android.essnotify.ServerAPIBase;
import eu.ess.ics.android.essnotify.backend.GetSubscriptionsTask;
import eu.ess.ics.android.essnotify.databinding.UserNotificationItemBinding;
import eu.ess.ics.android.essnotify.datamodel.Notification;
import eu.ess.ics.android.essnotify.datamodel.UserNotification;
import eu.ess.ics.android.essnotify.datamodel.UserService;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Adapter for items in the service settings list.
 */
public class MessagesListAdapter extends RecyclerView.Adapter<MessagesListAdapter.ViewHolder>
    implements MessageItemClickListener{

    private List<UserNotification> userNotifications;
    private Context context;
    private List<MessageRefreshCompletionListener> refreshCompleteionListeners
            = new ArrayList<>();

    private List<UserService> userServiceList;
    private Map<String, String> userServiceNames;

    public MessagesListAdapter() {
        // Instantiate with an empty list to avoid NPEs.
        this.userNotifications = new ArrayList<>();
    }

    public void addRefreshCompletionListener(MessageRefreshCompletionListener messageRefreshCompletionListener){
        refreshCompleteionListeners.add(messageRefreshCompletionListener);
    }

    public void removeRefreshCompletionListener(MessageRefreshCompletionListener messageRefreshCompletionListener){
        refreshCompleteionListeners.remove(messageRefreshCompletionListener);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        context = recyclerView.getContext();
        refresh();
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
        viewHolder.binding.setUserServiceNames(userServiceNames);
        viewHolder.binding.setItemClickListener(this);
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

    /**
     * First retrieves list if {@link UserService} such that service id can be mapped to
     * service name. Once completed the list of messages is retrieved.
     */
    public void refresh(){
        try {
            userServiceList = new GetSubscriptionsTask().execute(context).get();
            userServiceNames = mapServiceId2ServiceName(userServiceList);
            new GetMessagesTask().execute();
        } catch (Exception e) {
            // TODO: nothing? Or show error?
        }
    }

    private Map<String, String> mapServiceId2ServiceName(List<UserService> userServiceList){
        Map<String, String> map = new HashMap<>();
        userServiceList.stream().forEach(userService -> map.put(userService.getId(), userService.getCategory()));
        return map;
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
            } else {
                // TODO: if list cannot be retrieved, UI should show some error message.
            }
            refreshCompleteionListeners.stream().forEach(MessageRefreshCompletionListener::messagesRefreshed);
        }
    }

    @Override
    public void linkClicked(UserNotification userNotification){
        String url = userNotification.getUrl();
        if(url == null || url.isEmpty()){
            return;
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

    @Override
    public void messageClicked(View view, UserNotification userNotification){
        markAsRead(Arrays.asList(userNotification));
    }

    private class SetMessagesTask extends AsyncTask<List<Notification>, Void, Boolean>{
        @Override
        public Boolean doInBackground(List<Notification>... notifications) {
            BackendService backendService =
                    ServerAPIBase.getInstance().getBackendService(context);
            Call<Void> call = backendService.setNotifications(notifications[0]);
            try {
                call.execute();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    public void deleteAll(){

    }

    public void markAllAsRead(){
        markAsRead(userNotifications);
    }

    private void markAsRead(List<UserNotification> userNotifications){
        List<Notification> notifications =
                userNotifications.stream().map(un -> new Notification(un)).collect(Collectors.toList());
        try {
            if(new SetMessagesTask().execute(notifications).get()){
                userNotifications.stream().forEach(un -> un.setIs_read(true));
            }
        } catch (Exception e) {
            // TODO: Handle failure
        }
    }
}
