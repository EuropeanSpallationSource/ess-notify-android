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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import eu.ess.ics.android.essnotify.BR;
import eu.ess.ics.android.essnotify.backend.BackendService;
import eu.ess.ics.android.essnotify.R;
import eu.ess.ics.android.essnotify.ServerAPIBase;
import eu.ess.ics.android.essnotify.backend.GetSubscriptionsTask;
import eu.ess.ics.android.essnotify.databinding.ServiceItemBinding;
import eu.ess.ics.android.essnotify.datamodel.Service;
import eu.ess.ics.android.essnotify.datamodel.UserService;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Adapter for items in the service settings list.
 */
public class SettingsListAdapter extends RecyclerView.Adapter<SettingsListAdapter.ViewHolder>
        implements ServiceItemClickListener {

    private List<UserService> userServices;
    private List<UserService> filteredUserServices;
    private Context context;

    public SettingsListAdapter(){
        // Instantiate with an empty list to avoid NPEs.
        this.userServices = new ArrayList<>();
        this.filteredUserServices = userServices;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        context = recyclerView.getContext();
        //new GetSubscriptionsTask().execute(context);
        try {
            List<UserService> userServiceList =
                    new GetSubscriptionsTask().execute(context).get();
            if(userServiceList != null) {
                setServicesList(userServiceList);
            }
        } catch (Exception e) {
            // TODO: probably nothing.
        }
    }

    public void setServicesList(List<UserService> servicesList){
        this.userServices = servicesList;
        this.filteredUserServices = userServices;
        notifyDataSetChanged();
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
    public SettingsListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        ServiceItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(viewGroup.getContext()),
                R.layout.service_item, viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        UserService dataModel = filteredUserServices.get(position);
        viewHolder.bind(dataModel);
        viewHolder.binding.setItemClickListener(this);
    }

    @Override
    public int getItemCount() {
        return filteredUserServices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ServiceItemBinding binding;

        public ViewHolder(ServiceItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Object obj) {
            binding.setVariable(BR.model, obj);
            binding.executePendingBindings();
        }
    }

    @Override
    public void serviceItemClicked(View view, UserService userService){
        CheckBox checkBox = (CheckBox)view;
        Service service = new Service();
        service.setId(userService.getId());
        service.setIs_subscribed(checkBox.isChecked());

        new SetSubscriptionTask(Arrays.asList(service)).execute();
    }

    private class SetSubscriptionTask extends AsyncTask<Void, Void, Integer> {

        private List<Service> subscriptions;

        public SetSubscriptionTask(List<Service> subscriptions){
            this.subscriptions = subscriptions;
        }

        @Override
        public Integer doInBackground(Void... args) {
            BackendService backendService =
                    ServerAPIBase.getInstance().getBackendService(context);
            Call<Void> setSubscriptionCall = backendService.setSubscriptions(subscriptions);
            try {
                Response<Void> response = setSubscriptionCall.execute();
                return response.code();
            } catch (Exception e) {
                return -1;
            }
        }

        @Override
        public void onPostExecute(Integer httpStatus){
           if(httpStatus > -1 && httpStatus <= 300){
               try {
                   List<UserService> userServiceList =
                           new GetSubscriptionsTask().execute(context).get();
                   if(userServiceList != null) {
                       setServicesList(userServiceList);
                   }
               } catch (Exception e) {
                   // TODO: probably nothing.
               }
           }
        }
    }
}
