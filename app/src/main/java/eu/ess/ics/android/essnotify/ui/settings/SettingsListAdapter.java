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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import eu.ess.ics.android.essnotify.BR;
import eu.ess.ics.android.essnotify.R;
import eu.ess.ics.android.essnotify.databinding.ServiceItemBinding;
import eu.ess.ics.android.essnotify.datamodel.UserService;

public class SettingsListAdapter extends RecyclerView.Adapter<SettingsListAdapter.ViewHolder>
        implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "SettingsListAdapter";
    private List<UserService> userServices;
    private List<UserService> filteredUserServices;

    public SettingsListAdapter(){
        // Instantiate with an empty list to avoid NPEs.
        this.userServices = new ArrayList<>();
        this.filteredUserServices = userServices;
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

        binding.userServiceSelected.setOnCheckedChangeListener(this);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.d(TAG, "Element " + position + " set.");
        UserService dataModel = filteredUserServices.get(position);
        viewHolder.bind(dataModel);
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
    public void onCheckedChanged(CompoundButton checkBox, boolean checked) {
        Log.d(TAG, checkBox.getText().toString());
        Log.d(TAG, "Checked = " + checked);
    }
}
