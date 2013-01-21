/*
 * Copyright 2012 James Geboski <jgeboski@gmail.com>
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
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.ensifera.animosity.craftirc.CraftIRC;
import com.ensifera.animosity.craftirc.EndPoint;
import com.ensifera.animosity.craftirc.RelayedMessage;

import org.jgeboski.mcmmoirc.command.CIRCReload;
import org.jgeboski.mcmmoirc.point.AdminPoint;
import org.jgeboski.mcmmoirc.point.GamePoint;
import org.jgeboski.mcmmoirc.point.PartyPoint;
import org.jgeboski.mcmmoirc.util.Log;
import org.jgeboski.mcmmoirc.util.Message;

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

        adminPoint   = new AdminPoint(this);
        partyPoint   = new GamePoint(this);
        partyPoints  = new HashMap<String, PartyPoint>();
    }

    public void onEnable()
    {
        PluginCommand command;
        PluginManager pm;
        Plugin p;

        config.load();
        partyPoints.clear();

        pm = getServer().getPluginManager();
        p  = pm.getPlugin("CraftIRC");

        if ((p == null) || !p.isEnabled()) {
            Log.severe("Failed to find CraftIRC");
            setEnabled(false);
            return;
        }

        craftirc = (CraftIRC) p;

        command   = getServer().getPluginCommand("ircreload");
        ircrExec  = command.getExecutor();
        command.setExecutor(new CIRCReload(this));

        reload();
        events.register();
    }

    public void onDisable()
    {
        getServer().getPluginCommand("ircreload").setExecutor(ircrExec);

        craftirc.unregisterEndPoint(config.adminTag);
        craftirc.unregisterEndPoint(config.partyTag);

        for (PartyPoint v : partyPoints.values())
            craftirc.unregisterEndPoint(v.getTag());
    }

    public void reload()
    {
        PartyPoint partyp;
        String     party;
        String     tag;

        craftirc.unregisterEndPoint(config.adminTag);
        craftirc.unregisterEndPoint(config.partyTag);

        for (PartyPoint v : partyPoints.values())
            craftirc.unregisterEndPoint(v.getTag());

        config.load();

        registerEndPoint(config.adminTag, adminPoint);
        registerEndPoint(config.partyTag, partyPoint);

        for (Entry<String, String> e : config.parties.entrySet()) {
            party  = e.getKey();
            tag    = e.getValue();
            partyp = new PartyPoint(this, tag, party);

            if (registerEndPoint(tag, partyp))
                partyPoints.put(party, partyp);
        }
    }

    private boolean registerEndPoint(String tag, EndPoint ep)
    {
        if (craftirc.registerEndPoint(tag, ep))
            return true;

        Log.severe("Unable to register CraftIRC tag: %s", tag);
        return false;
    }

    private RelayedMessage fillMessage(RelayedMessage rmsg, String sender,
                                       String msg)
    {
        Player p;

        p = getServer().getPlayerExact(sender);

        if (p != null) {
            rmsg.setField("realSender", p.getName());
            rmsg.setField("sender",     p.getDisplayName());
            rmsg.setField("world",      p.getWorld().getName());
        } else {
            rmsg.setField("sender",     sender);
        }

        rmsg.setField("message", msg);
        return rmsg;
    }

    public void adminMessageToIRC(String sender, String event, String msg)
    {
        RelayedMessage rmsg;

        rmsg = craftirc.newMsg(adminPoint, null, event);
        rmsg = fillMessage(rmsg, sender, msg);

        rmsg.post();
    }

    public void partyMessageToIRC(String sender, String event, String party,
                                  String msg)
    {
        RelayedMessage rmsg;
        GamePoint      gp;

        gp = partyPoints.get(party);

        if (gp == null)
            gp = partyPoint;

        rmsg = craftirc.newMsg(gp, null, event);
        rmsg = fillMessage(rmsg, sender, msg);

        rmsg.setField("srcParty", party);
        rmsg.post();
    }
}
