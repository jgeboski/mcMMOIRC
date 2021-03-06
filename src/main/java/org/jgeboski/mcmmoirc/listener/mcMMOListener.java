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

package org.jgeboski.mcmmoirc.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import com.ensifera.animosity.craftirc.RelayedMessage;

import com.gmail.nossr50.events.chat.McMMOAdminChatEvent;
import com.gmail.nossr50.events.chat.McMMOPartyChatEvent;
import com.gmail.nossr50.events.party.McMMOPartyChangeEvent;

import org.jgeboski.mcmmoirc.mcMMOIRC;
import org.jgeboski.mcmmoirc.point.PartyPoint;

public class mcMMOListener implements Listener
{
    public mcMMOIRC mirc;

    public mcMMOListener(mcMMOIRC mirc)
    {
        this.mirc = mirc;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerChat(McMMOAdminChatEvent event)
    {
        RelayedMessage msg;
        Plugin         p;

        p = event.getPlugin();

        if ((p != null) && (p instanceof mcMMOIRC))
            return;

        msg = mirc.craftirc.newMsg(mirc.adminPoint, null, "chat");

        msg.setField("realSender", event.getSender());
        msg.setField("sender",     event.getDisplayName());
        msg.setField("message",    event.getMessage());

        mirc.registerEndPoint(mirc.config.adminTag, mirc.adminPoint);
        msg.post();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerChat(McMMOPartyChatEvent event)
    {
        RelayedMessage msg;
        PartyPoint     pp;
        Plugin         pl;

        pl = event.getPlugin();

        if ((pl != null) && (pl instanceof mcMMOIRC))
            return;

        pp = mirc.getPartyPoint(event.getParty());

        if (pp == null)
            return;

        msg = mirc.craftirc.newMsg(pp, null, "chat");

        msg.setField("realSender", event.getSender());
        msg.setField("sender",     event.getDisplayName());
        msg.setField("message",    event.getMessage());
        msg.setField("srcParty",   event.getParty());

        mirc.registerEndPoint(pp);
        msg.post();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPartyChange(McMMOPartyChangeEvent event)
    {
        Player p;

        p = event.getPlayer();

        switch (event.getReason()) {
        case JOINED_PARTY:
            partyChange("join", event.getNewParty(), p);
            break;

        case LEFT_PARTY:
            partyChange("part", event.getOldParty(), p);
            break;

        case KICKED_FROM_PARTY:
            partyChange("kick", event.getOldParty(), p);
            break;

        case CHANGED_PARTIES:
            partyChange("part", event.getOldParty(), p);
            partyChange("join", event.getNewParty(), p);
            break;

        default:
            return;
        }
    }

    private void partyChange(String type, String party, Player player)
    {
        RelayedMessage msg;
        PartyPoint     pp;

        pp = mirc.getPartyPoint(party);

        if (pp == null)
            return;

        msg = mirc.craftirc.newMsg(pp, null, type);

        msg.setField("realSender", player.getName());
        msg.setField("sender",     player.getDisplayName());
        msg.setField("srcParty",   party);

        mirc.registerEndPoint(pp);
        msg.post();
    }
}
