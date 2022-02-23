/*
 * Copyright (C) 2022 European Spallation Source ERIC.
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

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import eu.ess.ics.android.essnotify.R;
import eu.ess.ics.android.essnotify.datamodel.UserNotification;

public class MessageDetailsFragment extends BottomSheetDialogFragment {


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message_details, container, false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
                setupFullHeight(bottomSheetDialog);
            }
        });
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MessageDetailsViewModel model =
                new ViewModelProvider(requireActivity()).get(MessageDetailsViewModel.class);
        model.getSelected().observe(getViewLifecycleOwner(), item -> {
            ((TextView)view.findViewById(R.id.serviceName)).setText(item.getServiceName());
            ((TextView)view.findViewById(R.id.title)).setText(item.getUserNotification().getTitle());
            ((TextView)view.findViewById(R.id.date)).setText(UserNotification.formatDate(item.getUserNotification().getTimestamp()));
            ((TextView)view.findViewById(R.id.body)).setText(MessagesListAdapter.getHtml(item.getUserNotification().getSubtitle()));
            ((TextView)view.findViewById(R.id.url)).setText(item.getUserNotification().getUrl());
            view.findViewById(R.id.notificationHeader).setBackground(item.getHeaderColor());
        });

        TextView url = (TextView)view.findViewById(R.id.url);
        url.setMovementMethod(LinkMovementMethod.getInstance());

        view.findViewById(R.id.shareIcon).setOnClickListener(e -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append( ((TextView)view.findViewById(R.id.title)).getText()).append(System.lineSeparator()).append(System.lineSeparator());
            stringBuilder.append( ((TextView)view.findViewById(R.id.date)).getText()).append(System.lineSeparator()).append(System.lineSeparator());
            stringBuilder.append( ((TextView)view.findViewById(R.id.url)).getText()).append(System.lineSeparator()).append(System.lineSeparator());
            stringBuilder.append( ((TextView)view.findViewById(R.id.body)).getText());
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString());
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        });
    }

    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        FrameLayout bottomSheet = (FrameLayout) bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();

        int windowHeight = getWindowHeight();
        if (layoutParams != null) {
            layoutParams.height = windowHeight;
        }
        bottomSheet.setLayoutParams(layoutParams);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }
}
