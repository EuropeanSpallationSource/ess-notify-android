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

/**
 * Describes a notification service to which the user may subscribe.
 */
public class UserService {

    private String category;
    private String color;
    private String owner;
    private String id;
    private boolean is_subscribed;

    private static final String COLOR_FORMAT = "^([0-9a-fA-F]{2}){3}$";

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        if(!color.trim().matches(COLOR_FORMAT)){
            this.color = "000000";
        }
        else{
            this.color = color.trim();
        }
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isIs_subscribed() {
        return is_subscribed;
    }

    public void setIs_subscribed(boolean is_subscribed) {
        this.is_subscribed = is_subscribed;
    }
}
