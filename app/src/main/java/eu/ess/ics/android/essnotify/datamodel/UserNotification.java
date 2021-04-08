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

package eu.ess.ics.android.essnotify.datamodel;

import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;

import eu.ess.ics.android.essnotify.BR;
import eu.ess.ics.android.essnotify.Constants;

/**
 * Holds data describing a notification.
 */

public class UserNotification extends BaseObservable {

    private int id;
    private String title;
    private String subtitle;
    private String url;
    private String timestamp;
    private String service_id;
    private boolean is_read;
    private int visibility;

    @Bindable
    @JsonIgnore
    public int getVisibility(){
        return visibility;
    }

    @JsonIgnore
    public void setVisibility(int visibility){
        this.visibility = visibility;
        notifyPropertyChanged(BR.visibility);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getService_id() {
        return service_id;
    }

    public void setService_id(String service_id) {
        this.service_id = service_id;
    }

    public boolean isIs_read() {
        return is_read;
    }

    public void setIs_read(boolean is_read) {
        this.is_read = is_read;
        this.visibility = is_read ? View.GONE : View.VISIBLE;
        notifyPropertyChanged(BR.visibility);
    }

    public static String formatDate(String originalDate){
        try {
            SimpleDateFormat simpleDateFormat = Constants.ORIGINAL_DATE_FORMAT;
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = simpleDateFormat.parse(originalDate);
            TimeZone tz = TimeZone.getDefault();
            simpleDateFormat.setTimeZone(tz);
            return Constants.DATE_FORMAT.format(date);
        } catch (Exception e) {
            return originalDate;
        }
    }
}
