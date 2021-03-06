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

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.ensifera.animosity.craftirc.CraftIRC;
import com.ensifera.animosity.craftirc.EndPoint;

import org.jgeboski.mcmmoirc.command.CmcMMOIRC;
import org.jgeboski.mcmmoirc.listener.mcMMOListener;
import org.jgeboski.mcmmoirc.point.AdminPoint;
import org.jgeboski.mcmmoirc.point.PartyPoint;
import org.jgeboski.mcmmoirc.util.Log;
import org.jgeboski.mcmmoirc.util.Message;

public class mcMMOIRC extends JavaPlugin
{
    public Configuration config;
    public AdminPoint    adminPoint;
    public CraftIRC      craftirc;

    public void onLoad()
    {
        config     = new Configuration(new File(getDataFolder(), "config.yml"));
        adminPoint = new AdminPoint(this);
    }

    public void onEnable()
    {
        PluginManager pm;
        Plugin        p;
        PartyPoint    pp;
        Party         pt;

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
        config.load();
        craftirc.registerEndPoint(config.adminTag, adminPoint);

        for (Entry<String, Party> e : config.parties.entrySet()) {
            pt  = e.getValue();
            pp = (PartyPoint) craftirc.getEndPoint(pt.tag);

            if (pp == null)
                pp = new PartyPoint(this);

            if (registerEndPoint(pt.tag, pp))
                pp.parties.add(pt);
        }

        pm.registerEvents(new mcMMOListener(this), this);
        getCommand("mcmmoirc").setExecutor(new CmcMMOIRC(this));
    }

    public void onDisable()
    {
        for (Entry<String, Party> e : config.parties.entrySet())
            craftirc.unregisterEndPoint(e.getValue().tag);

        craftirc.unregisterEndPoint(config.adminTag);
    }

    public void reload()
    {
        PluginManager pm;

        pm = getServer().getPluginManager();

        pm.disablePlugin(this);
        pm.enablePlugin(this);
    }

    public boolean registerEndPoint(String tag, Object ep)
    {
        if (craftirc == null)
            return false;

        /* Prevent CraftIRC from dispatching an error message */
        if (craftirc.getEndPoint(tag) != null)
            return true;

        if (craftirc.registerEndPoint(tag, (EndPoint) ep))
            return true;

        Log.severe("Failed to register CraftIRC tag: %s", tag);
        return false;
    }

    public void registerEndPoint(PartyPoint pp)
    {
        for (Party p : pp.parties)
            registerEndPoint(p.tag, pp);
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
