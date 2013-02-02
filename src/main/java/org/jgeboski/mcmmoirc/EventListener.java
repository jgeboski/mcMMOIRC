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

package org.jgeboski.mcmmoirc;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.ensifera.animosity.craftirc.EndPoint;
import com.ensifera.animosity.craftirc.RelayedMessage;

import com.gmail.nossr50.events.chat.McMMOAdminChatEvent;
import com.gmail.nossr50.events.chat.McMMOPartyChatEvent;

import org.jgeboski.mcmmoirc.point.PartyPoint;

public class EventListener implements Listener
{
    public mcMMOIRC mirc;

    public EventListener(mcMMOIRC mirc)
    {
        this.mirc = mirc;
    }

    public void register()
    {
        PluginManager pm;

        pm = mirc.getServer().getPluginManager();
        pm.registerEvents(this, mirc);
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

        msg.post();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerChat(McMMOPartyChatEvent event)
    {
        RelayedMessage msg;
        EndPoint       ep;
        Party          pt;
        Plugin         p;

        p = event.getPlugin();

        if ((p != null) && (p instanceof mcMMOIRC))
            return;

        pt = mirc.config.parties.get(event.getParty());
        ep = mirc.craftirc.getEndPoint(pt.tag);

        if (ep == null)
            return;

        msg = mirc.craftirc.newMsg(ep, null, "chat");

        msg.setField("realSender", event.getSender());
        msg.setField("sender",     event.getDisplayName());
        msg.setField("message",    event.getMessage());
        msg.setField("srcParty",   pt.name);

        msg.post();
    }
}
