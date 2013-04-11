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

import java.io.File;
import java.util.Map.Entry;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.ensifera.animosity.craftirc.CraftIRC;
import com.ensifera.animosity.craftirc.EndPoint;

import org.jgeboski.mcmmoirc.command.CIRCReload;
import org.jgeboski.mcmmoirc.point.AdminPoint;
import org.jgeboski.mcmmoirc.point.PartyPoint;
import org.jgeboski.mcmmoirc.util.Log;
import org.jgeboski.mcmmoirc.util.Message;

public class mcMMOIRC extends JavaPlugin
{
    public Configuration   config;
    public AdminPoint      adminPoint;
    public CraftIRC        craftirc;
    public CommandExecutor ircrExec;

    private EventListener events;

    public void onLoad()
    {
        config = new Configuration(new File(getDataFolder(), "config.yml"));
        events = new EventListener(this);

        adminPoint = new AdminPoint(this);
    }

    public void onEnable()
    {
        PluginCommand command;
        PluginManager pm;
        Plugin        p;

        Log.init(getLogger());
        Message.init(getDescription().getName());

        pm = getServer().getPluginManager();
        p  = pm.getPlugin("CraftIRC");

        if ((p == null) || !p.isEnabled()) {
            Log.severe("Failed to find CraftIRC");
            setEnabled(false);
            return;
        }

        craftirc = (CraftIRC) p;

        reload();
        events.register();

        command  = getServer().getPluginCommand("ircreload");
        ircrExec = command.getExecutor();
        command.setExecutor(new CIRCReload(this));
    }

    public void onDisable()
    {
        Party p;

        getServer().getPluginCommand("ircreload").setExecutor(ircrExec);
        craftirc.unregisterEndPoint(config.adminTag);

        for (Entry<String, Party> e : config.parties.entrySet()) {
            p = e.getValue();
            craftirc.unregisterEndPoint(p.tag);
        }
    }

    public void reload()
    {
        PartyPoint pp;
        Party      p;

        craftirc.unregisterEndPoint(config.adminTag);

        for (Entry<String, Party> e : config.parties.entrySet()) {
            p = e.getValue();
            craftirc.unregisterEndPoint(p.tag);
        }

        config.load();
        craftirc.registerEndPoint(config.adminTag, adminPoint);

        for (Entry<String, Party> e : config.parties.entrySet()) {
            p  = e.getValue();
            pp = (PartyPoint) craftirc.getEndPoint(p.tag);

            if (pp != null) {
                pp.parties.add(p);
                continue;
            }

            pp = new PartyPoint(this);

            if (craftirc.registerEndPoint(p.tag, pp)) {
                pp.parties.add(p);
                continue;
            }

            Log.severe("Unable to register CraftIRC tag: %s", p.tag);
        }
    }

    public PartyPoint getPartyPoint(String party)
    {
        Party p;

        p = config.parties.get(party);

        if (p == null)
            return null;

        return (PartyPoint) craftirc.getEndPoint(p.tag);
    }
}
