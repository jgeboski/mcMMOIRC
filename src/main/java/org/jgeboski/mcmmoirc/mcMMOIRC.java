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
import java.util.HashMap;
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
import org.jgeboski.mcmmoirc.point.GamePoint;
import org.jgeboski.mcmmoirc.point.PartyPoint;
import org.jgeboski.mcmmoirc.util.Log;

public class mcMMOIRC extends JavaPlugin
{
    public static String pluginName = "mcMMOIRC";

    public Configuration config;
    public CraftIRC      craftirc;

    private EventListener events;

    public AdminPoint adminPoint;
    public GamePoint  partyPoint;
    public HashMap<String, PartyPoint> partyPoints;

    public CommandExecutor ircrExec;

    public void onLoad()
    {
        config = new Configuration(new File(getDataFolder(), "config.yml"));
        events = new EventListener(this);

        adminPoint  = new AdminPoint(this);
        partyPoint  = new GamePoint(this);
        partyPoints = new HashMap<String, PartyPoint>();
    }

    public void onEnable()
    {
        PluginCommand command;
        PluginManager pm;
        Plugin p;

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
        getServer().getPluginCommand("ircreload").setExecutor(ircrExec);

        craftirc.unregisterEndPoint(config.adminTag);
        craftirc.unregisterEndPoint(config.partyTag);

        for (PartyPoint pp : partyPoints.values())
            craftirc.unregisterEndPoint(pp.party.tag);
    }

    public void reload()
    {
        PartyPoint pp;

        craftirc.unregisterEndPoint(config.adminTag);
        craftirc.unregisterEndPoint(config.partyTag);

        for (PartyPoint pv : partyPoints.values())
            craftirc.unregisterEndPoint(pv.party.tag);

        partyPoints.clear();
        config.load();

        registerEndPoint(config.adminTag, adminPoint);
        registerEndPoint(config.partyTag, partyPoint);

        for (Entry<String, Party> e : config.parties.entrySet()) {
            pp = new PartyPoint(this, e.getValue());

            if (registerEndPoint(pp.party.tag, pp))
                partyPoints.put(pp.party.name, pp);
        }
    }

    private boolean registerEndPoint(String tag, EndPoint ep)
    {
        if (craftirc.registerEndPoint(tag, ep))
            return true;

        Log.severe("Unable to register CraftIRC tag: %s", tag);
        return false;
    }
}
