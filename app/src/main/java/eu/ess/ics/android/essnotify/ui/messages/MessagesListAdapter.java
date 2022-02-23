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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

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
import eu.ess.ics.android.essnotify.ui.BackendErrorHelper;
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
    private final List<MessageRefreshCompletionListener> refreshCompleteionListeners
            = new ArrayList<>();

    private List<UserService> userServiceList;
    private Map<String, String> userServiceNames;
    private Map<String, Drawable> userServiceColors;

    private String currentFilter;
    private static Parser parser;
    private static HtmlRenderer htmlRenderer;
    private RecyclerView recyclerView;
    private MessagesFragment.LaunchDetailView launcher;

    static {
        parser = Parser.builder().build();
        htmlRenderer = HtmlRenderer.builder().build();
    }

    public MessagesListAdapter(MessagesFragment.LaunchDetailView launcher) {
        // Instantiate with an empty list to avoid NPEs.
        this.userNotifications = new ArrayList<>();
        this.filteredUserNotifications = userNotifications;
        this.launcher = launcher;
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
        refresh(true);
        this.recyclerView = recyclerView;
    }

    public Context getContext() {
        return context;
    }

    /**
     * Filters current list of messages and requests UI update.
     */
    private void setNotifictions() {
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
        viewHolder.itemView.findViewById(R.id.notificationHeader).setBackground(userServiceColors.get(dataModel.getService_id()));
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

    /**
     * Delete a single message, e.g. when user swipes left in the message list view.
     *
     * @param position Position in the currently displayed message list.
     */
    public void deleteMessage(int position) {
        UserNotification userNotification = filteredUserNotifications.get(position);
        filteredUserNotifications.remove(position);
        notifyItemRemoved(position);
        if (delete(Arrays.asList(userNotification))) {
            refresh(false);
        }
    }

    /**
     * Deletes all messages currently in the view. When back-end acknowledges the list
     * view is refreshed, which in turn may show older messages that have not yet been
     * deleted by user.
     */
    public void deleteAllMessages() {
        if (delete(filteredUserNotifications)) {
            filteredUserNotifications.clear();
            notifyDataSetChanged();
            refresh(false);
        }
    }

    /**
     * First retrieves list if {@link UserService} such that service id can be mapped to
     * service name. Once completed the list of messages is retrieved.
     */
    public void refresh(boolean showNetworkError) {
        try {
            userServiceList = new GetSubscriptionsTask().execute(context).get();
            if (userServiceList == null) {
                if (showNetworkError) {
                    BackendErrorHelper.showNetworkErrorDialog(context);
                }
                refreshCompleteionListeners.stream().forEach(MessageRefreshCompletionListener::messagesRefreshed);
                return;
            }
            userServiceNames = mapServiceId2ServiceName(userServiceList);
            userServiceColors = mapServiceId2ServiceColor(userServiceList);
            new GetMessagesTask().execute();
        } catch (Exception e) {
            if (showNetworkError) {
                BackendErrorHelper.showNetworkErrorDialog(context);
            }
        }
    }

    /**
     * Helper method mapping service id to service name.
     *
     * @param userServiceList The full list of services.
     * @return A {@link Map} where key is service id and value is service name.
     */
    private Map<String, String> mapServiceId2ServiceName(List<UserService> userServiceList) {
        Map<String, String> map = new HashMap<>();
        userServiceList.stream().forEach(userService -> map.put(userService.getId(), userService.getCategory()));
        return map;
    }

    /**
     * Helper method mapping service id to service name.
     *
     * @param userServiceList The full list of services.
     * @return A {@link Map} where key is service id and value is service name.
     */
    private Map<String, Drawable> mapServiceId2ServiceColor(List<UserService> userServiceList) {
        Map<String, Drawable> map = new HashMap<>();
        userServiceList.stream()
                .forEach(userService -> {
                    try {
                        map.put(userService.getId(), new ColorDrawable(Color.parseColor("#FF" + userService.getColor())));
                    } catch (Exception e) {
                        // In case color on server is not a valid hexadecimal string...
                        map.put(userService.getId(), new ColorDrawable(Color.parseColor("#FF000000")));
                    }
                });
        return map;
    }

    /**
     * An {@link AsyncTask} retrieving messages from the back-end service. If successful, the
     * call will update the message view, i.e. calling code need not do it.
     */
    private class GetMessagesTask extends AsyncTask<Void, Void, List<UserNotification>> {

        @Override
        public List<UserNotification> doInBackground(Void... args) {
            BackendService backendService =
                    ServerAPIBase.getInstance().getBackendService(context);
            Call<List<UserNotification>> call = backendService.getNotifications();
            try {
                Response<List<UserNotification>> response = call.execute();
                if (response.code() == 401) {
                    BackendErrorHelper.goToLogin(getContext());
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

    /**
     * Handles click on link icon in message header. The view should display the link icon
     * only if there is a non-empty URL field ({@link UserNotification#getUrl()} in the message, so there is no need for
     * additional checks of the URL.
     *
     * @param userNotification
     */
    @Override
    public void linkClicked(UserNotification userNotification) {
        String url = userNotification.getUrl();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

    /**
     * Handles click on a message. The wanted outcome is to mark the message a read (i.e. to remove
     * red dot next to title) and to toggle the text view size. By default the text view shows
     * 3 lines, but when toggled expands to at most 100 lines. This way a user may still read a longer
     * message and then collapse the text view when done.
     *
     * @param view
     * @param userNotification
     */
    @Override
    public void messageClicked(View view, UserNotification userNotification) {
        markAsRead(Arrays.asList(userNotification));
        launcher.launch(
                new MessageDetailsData(userNotification, userServiceNames.get(userNotification.getService_id()), userServiceColors.get(userNotification.getService_id())));
    }

    /**
     * An {@link AsyncTask} to set the "status" of a message, i.e. "read" or "deleted". The
     * status is sent to the remote service in order to keep data consistent.
     */
    private class SetMessagesTask extends AsyncTask<List<Notification>, Void, Boolean> {
        @Override
        public Boolean doInBackground(List<Notification>... notifications) {
            BackendService backendService =
                    ServerAPIBase.getInstance().getBackendService(context);
            Call<Void> call = backendService.setNotifications(notifications[0]);
            try {
                Response<Void> response = call.execute();
                if (response.code() == 401) {
                    BackendErrorHelper.goToLogin(context);
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    /**
     * Deletes messages on the remote service, i.e. sends status "deleted".
     *
     * @param userNotifications
     * @return
     */
    public boolean delete(List<UserNotification> userNotifications) {
        // Copy list of notifications and set status="read" for each of them.
        List<Notification> notifications =
                userNotifications.stream().map(un -> new Notification(un, "deleted")).collect(Collectors.toList());
        boolean deleteOk = false;
        try {
            deleteOk = new SetMessagesTask().execute(notifications).get();
            if (!deleteOk) {
                BackendErrorHelper.showNetworkErrorDialog(context);
            }
        } catch (Exception e) {
            // Ignore
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

    private void filter() {
        if (currentFilter.isEmpty() || currentFilter.equals(getContext().getResources().getString(R.string.all))) {
            filteredUserNotifications = userNotifications;
            return;
        }
        String serviceId = userServiceList.stream().filter(us -> us.getCategory().equals(currentFilter)).findFirst().get().getId();
        filteredUserNotifications =
                userNotifications.stream().filter(un -> un.getService_id().equals(serviceId)).collect(Collectors.toList());
    }


    public void showFilterDialog(FragmentManager fragmentManager) {
        // userServiceList may be null if app was launched when off-line
        if (userServiceList == null || userServiceList.isEmpty()) {
            return;
        }
        List<UserService> currentSubscriptions =
                userServiceList.stream().filter(u -> u.isIs_subscribed()).collect(Collectors.toList());
        MessageFilterDialogFragment dialog = new MessageFilterDialogFragment(this, currentSubscriptions, currentFilter);
        dialog.show(fragmentManager, "NoticeDialogFragment");
    }

    public void applyFilter(String filter) {
        currentFilter = filter;
        setNotifictions();
    }

    public static Spanned getHtml(String commonmarkString) {
        org.commonmark.node.Node document = parser.parse(commonmarkString);
        String html = htmlRenderer.render(document);
        Spanned spanned = Html.fromHtml(html, 0);
        return spanned;
    }
}
