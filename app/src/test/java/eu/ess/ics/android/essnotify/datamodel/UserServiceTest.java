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

import org.junit.Test;
import static org.junit.Assert.*;

public class UserServiceTest {

    @Test
    public void testSetColor(){
        UserService userService = new UserService();

        userService.setColor("AA0000");
        assertEquals("AA0000", userService.getColor());

        userService.setColor("00ff00");
        assertEquals("00ff00", userService.getColor());


        userService.setColor("AA00pp");
        assertEquals("000000", userService.getColor());

        userService.setColor("#AA0000");
        assertEquals("000000", userService.getColor());

        userService.setColor("AA000");
        assertEquals("000000", userService.getColor());
    }
}
