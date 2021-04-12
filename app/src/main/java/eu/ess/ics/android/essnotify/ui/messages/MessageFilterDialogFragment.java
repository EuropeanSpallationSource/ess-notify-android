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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.List;

import eu.ess.ics.android.essnotify.R;
import eu.ess.ics.android.essnotify.datamodel.UserService;

/**
 * Dialog showing the list of services to which the user has subscribed. User may check/uncheck
 * items to define a filter to be used by message view. The special item "All" (on top) is
 * of course used to indicate that messages should not be filtered in the view.
 */
public class MessageFilterDialogFragment extends DialogFragment {

    private String currentFilter;
    private List<UserService> currentSubscriptions;
    private int selectedIndex;
    private MessagesListAdapter messagesListAdapter;

    public MessageFilterDialogFragment(MessagesListAdapter messagesListAdapter, List<UserService> currentSubscriptions, String currentFilter){
        this.messagesListAdapter = messagesListAdapter;
        this.currentFilter = currentFilter;
        this.currentSubscriptions = currentSubscriptions;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        currentSubscriptions.sort((u1, u2) -> u1.getCategory().compareTo(u2.getCategory()));
        CharSequence[] choices = new CharSequence[currentSubscriptions.size() + 1];

        choices[0] = getResources().getString(R.string.all);

        for(int i = 0; i < currentSubscriptions.size(); i++){
            choices[i + 1] = currentSubscriptions.get(i).getCategory();
            if(currentSubscriptions.get(i).getCategory().equals(currentFilter)){
                selectedIndex = i + 1;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
        builder.setTitle(R.string.filter_messages)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setSingleChoiceItems(choices, selectedIndex,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                messagesListAdapter.applyFilter(choices[which].toString());
                                dismiss();
                            }
                        })
                // Set the action buttons
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private boolean[] computeChecked(List<UserService> serviceList, List<String> currentFilter){
        boolean[] checked = new boolean[serviceList.size() + 1];
        if(currentFilter.isEmpty()){
            checked[0] = true;
        }
        return checked;
    }

    public String getSelection(){
        return currentFilter;
    }
}
