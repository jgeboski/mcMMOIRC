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

import java.util.ArrayList;

import com.ensifera.animosity.craftirc.RelayedMessage;

import com.gmail.nossr50.api.ChatAPI;
import com.gmail.nossr50.party.PartyManager;

import org.jgeboski.mcmmoirc.mcMMOIRC;
import org.jgeboski.mcmmoirc.Party;

public class PartyPoint extends GamePoint
{
    public ArrayList<Party> parties;

    public PartyPoint(mcMMOIRC mirc)
    {
        super(mirc);
        this.parties = new ArrayList<Party>();
    }

    public void messageIn(RelayedMessage msg)
    {
        String s;
        String m;
        String d;

        s = msg.getField("sender");
        m = msg.getField("message");

        for (Party p : parties) {
            /* ChatAPI does not check party name validity */
            if (!PartyManager.isParty(p.name))
                continue;

            if (p.prefix != null)
                d = p.prefix + s;
            else
                d = s;

            if (p.suffix != null)
                d += p.suffix;

            ChatAPI.sendPartyChat(mirc, s, d, p.name, m);
        }
    }
}
