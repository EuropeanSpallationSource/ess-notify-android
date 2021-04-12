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

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import eu.ess.ics.android.essnotify.ui.LoginActivityRedirect;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Adapter for items in the service settings list.
 */
public class MessagesListAdapter extends RecyclerView.Adapter<MessagesListAdapter.ViewHolder>
        implements MessageItemClickListener {

    /**
     * The list of messages as retrieved from server on refresh. A refresh is implicit
     * when user wipes down, or implicit when user deletes items from the view.
     */
    private List<UserNotification> userNotifications;
    /**
     * List of filtered messages. These are the ones actually visible in the {@link android.widget.ListView}.
     * "Mark all as read" and "Delete all" actions will affect only the items in this list,
     * i.e. the {@link #userNotifications} list may contain additional items.
     */
    private List<UserNotification> filteredUserNotifications;
    private Context context;
    private List<MessageRefreshCompletionListener> refreshCompleteionListeners
            = new ArrayList<>();

    private List<UserService> userServiceList;
    private Map<String, String> userServiceNames;

    private String currentFilter;

    public MessagesListAdapter() {
        // Instantiate with an empty list to avoid NPEs.
        this.userNotifications = new ArrayList<>();
        this.filteredUserNotifications = userNotifications;
    }

    public void addRefreshCompletionListener(MessageRefreshCompletionListener messageRefreshCompletionListener) {
        refreshCompleteionListeners.add(messageRefreshCompletionListener);
    }

    public void removeRefreshCompletionListener(MessageRefreshCompletionListener messageRefreshCompletionListener) {
        refreshCompleteionListeners.remove(messageRefreshCompletionListener);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        context = recyclerView.getContext();
        currentFilter = getContext().getResources().getString(R.string.all);
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeToDeleteCallback(this));
        itemTouchHelper.attachToRecyclerView(recyclerView);
        refresh();
    }

    public Context getContext() {
        return context;
    }

    public void setNotifictions() {
        filter();
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
        UserNotification dataModel = filteredUserNotifications.get(position);
        viewHolder.bind(dataModel);
        viewHolder.binding.setUserServiceNames(userServiceNames);
        viewHolder.binding.setItemClickListener(this);
    }

    @Override
    public int getItemCount() {
        return filteredUserNotifications.size();
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

    public void deleteMessage(int position) {
        UserNotification userNotification = filteredUserNotifications.get(position);
        if (delete(Arrays.asList(userNotification))) {
            filteredUserNotifications.remove(position);
            notifyItemRemoved(position);
            refresh();
        }
    }

    public void deleteAllMessages() {
        if (delete(filteredUserNotifications)) {
            filteredUserNotifications.clear();
            notifyDataSetChanged();
            refresh();
        }
    }

    /**
     * First retrieves list if {@link UserService} such that service id can be mapped to
     * service name. Once completed the list of messages is retrieved.
     */
    public void refresh() {
        try {
            userServiceList = new GetSubscriptionsTask().execute(context).get();
            if(userServiceList == null){
                return;
            }
            userServiceNames = mapServiceId2ServiceName(userServiceList);
            new GetMessagesTask().execute();
        } catch (Exception e) {
            // TODO: nothing? Or show error?
        }
    }

    private Map<String, String> mapServiceId2ServiceName(List<UserService> userServiceList) {
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
                if(response.code() == 401){
                    LoginActivityRedirect.goToLogin(getContext());
                    return null;
                }
                return response.body();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public void onPostExecute(List<UserNotification> notifications) {
            if (notifications != null) {
                notifications.sort((u1, u2) -> u2.getTimestamp().compareTo(u1.getTimestamp()));
                MessagesListAdapter.this.userNotifications = notifications;
                setNotifictions();
            } else {
                // TODO: if list cannot be retrieved, UI should show some error message.
                return;
            }
            refreshCompleteionListeners.stream().forEach(MessageRefreshCompletionListener::messagesRefreshed);
        }
    }

    @Override
    public void linkClicked(UserNotification userNotification) {
        String url = userNotification.getUrl();
        if (url == null || url.isEmpty()) {
            return;
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

    @Override
    public void messageClicked(View view, UserNotification userNotification) {
        markAsRead(Arrays.asList(userNotification));
        TextView bodyText = view.findViewById(R.id.bodyText);
        userNotification.setExpanded(!userNotification.getExpanded());
        ObjectAnimator animation = ObjectAnimator.ofInt(
                bodyText,
                "maxLines",
                userNotification.getExpanded() ? 100 : 3);
        animation.setDuration(300);
        animation.start();
    }

    private class SetMessagesTask extends AsyncTask<List<Notification>, Void, Boolean> {
        @Override
        public Boolean doInBackground(List<Notification>... notifications) {
            BackendService backendService =
                    ServerAPIBase.getInstance().getBackendService(context);
            Call<Void> call = backendService.setNotifications(notifications[0]);
            try {
                Response<Void> response = call.execute();
                if(response.code() == 401){
                    LoginActivityRedirect.goToLogin(context);
                    return false;
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    public boolean delete(List<UserNotification> userNotifications) {
        // Copy list of notifications and set status="read" for each of them.
        List<Notification> notifications =
                userNotifications.stream().map(un -> new Notification(un, "deleted")).collect(Collectors.toList());
        boolean deleteOk = false;
        try {
            deleteOk = new SetMessagesTask().execute(notifications).get();
            if (!deleteOk) {
                // TODO: deletion failed, show error message
            }
        } catch (Exception e) {
            // TODO: Handle failure
        }
        return deleteOk;
    }

    public void markAllAsRead() {
        markAsRead(filteredUserNotifications);
    }

    private void markAsRead(List<UserNotification> userNotifications) {
        // Copy list of notifications and set status="read" for each of them.
        List<Notification> notifications =
                userNotifications.stream().map(un -> new Notification(un, "read")).collect(Collectors.toList());
        try {
            if (new SetMessagesTask().execute(notifications).get()) {
                userNotifications.stream().forEach(un -> un.setIs_read(true));
            }
        } catch (Exception e) {
            // TODO: Handle failure
        }
    }

    private void filter(){
        if(currentFilter.isEmpty() || currentFilter.equals(getContext().getResources().getString(R.string.all))){
            filteredUserNotifications = userNotifications;
            return;
        }
        String serviceId = userServiceList.stream().filter(us -> us.getCategory().equals(currentFilter)).findFirst().get().getId();
        filteredUserNotifications =
                userNotifications.stream().filter(un -> un.getService_id().equals(serviceId)).collect(Collectors.toList());
    }


    public void showFilterDialog(FragmentManager fragmentManager){
        List<UserService> currentSubscriptions =
                userServiceList.stream().filter(u -> u.isIs_subscribed()).collect(Collectors.toList());
        MessageFilterDialogFragment dialog = new MessageFilterDialogFragment(this, currentSubscriptions, currentFilter);
        dialog.show(fragmentManager, "NoticeDialogFragment");
    }

    public void applyFilter(String filter) {
        currentFilter = filter;
        setNotifictions();
    }
}
