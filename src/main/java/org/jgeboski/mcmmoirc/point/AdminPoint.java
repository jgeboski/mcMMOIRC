/*
 * Copyright 2012-2013 James Geboski <jgeboski@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jgeboski.mcmmoirc.point;

import com.ensifera.animosity.craftirc.RelayedMessage;
import com.gmail.nossr50.api.ChatAPI;
import org.jgeboski.mcmmoirc.mcMMOIRC;

public class AdminPoint extends GamePoint
{
    public AdminPoint(mcMMOIRC mirc)
    {
        super(mirc);
    }

    public void messageIn(RelayedMessage msg)
    {
        String s;
        String d;

        s = msg.getField("sender");

        if (mirc.config.adminPrefix != null)
            d = mirc.config.adminPrefix + s;
        else
            d = s;

        if (mirc.config.adminSuffix != null)
            d += mirc.config.adminSuffix;

        ChatAPI.sendAdminChat(mirc, s, d, msg.getField("message"));
    }
}
