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

package org.mcmmoirc;

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
import com.ensifera.animosity.craftirc.RelayedMessage;

import com.gmail.nossr50.datatypes.PlayerProfile;
import com.gmail.nossr50.mcPermissions;
import com.gmail.nossr50.Users;

import org.mcmmoirc.command.CA;
import org.mcmmoirc.command.CmcMMOIRC;
import org.mcmmoirc.command.CMe;
import org.mcmmoirc.command.CP;
import org.mcmmoirc.point.AdminPoint;
import org.mcmmoirc.point.GamePoint;
import org.mcmmoirc.point.PartyPoint;

public class mcMMOIRC extends JavaPlugin
{
    public static String pluginName = "mcMMOIRC";
    
    public Configuration config;
    public CraftIRC craftirc;
    
    public AdminPoint adminPoint;
    public GamePoint gamePoint;
    public HashMap<String, PartyPoint> partyPoints;
    
    public CommandExecutor adminExec;
    public CommandExecutor meExec;
    public CommandExecutor partyExec;
    
    private EventListener events;
    
    public void onLoad()
    {
        config = new Configuration(new File(getDataFolder(), "config.yml"));
        events = new EventListener(this);
        
        adminPoint  = new AdminPoint(this);
        gamePoint   = new GamePoint();
        partyPoints = new HashMap<String, PartyPoint>();
    }
    
    public void onEnable()
    {
        PluginCommand command;
        PluginManager pm;
        Plugin plugin;
        
        String party, tag;
        PartyPoint partyp;
        
        config.load();
        partyPoints.clear();
        
        pm     = getServer().getPluginManager();
        plugin = pm.getPlugin("CraftIRC");
        
        if(plugin == null) {
            Log.severe("Unable to find CraftIRC");
            return;
        }
        
        craftirc = (CraftIRC) plugin;
        
        if(!craftirc.registerEndPoint(config.adminTag, adminPoint))
            Log.severe("Unable to register CraftIRC tag: %s", config.adminTag);
        
        if(!craftirc.registerEndPoint(config.gameTag, gamePoint))
            Log.severe("Unable to register CraftIRC tag: %s", config.gameTag);
        
        for(Entry<String, String> e : config.parties.entrySet()) {
            party  = e.getKey();
            tag    = e.getValue();
            partyp = new PartyPoint(this, tag, party);
            
            Log.info("Registering: %s: %s", party, tag);
            
            if(craftirc.registerEndPoint(tag, partyp))
                partyPoints.put(party, partyp);
            else
                Log.severe("Unable to register CraftIRC tag: %s", tag);
        }
        
        command   = getServer().getPluginCommand("a");
        adminExec = command.getExecutor();
        command.setExecutor(new CA(this));
        
        command = getServer().getPluginCommand("me");
        meExec  = command.getExecutor();
        command.setExecutor(new CMe(this));
        
        command   = getServer().getPluginCommand("p");
        partyExec = command.getExecutor();
        command.setExecutor(new CP(this));
        
        getCommand("mcmmoirc").setExecutor(new CmcMMOIRC(this));
        events.register();
    }
    
    public void onDisable()
    {
        PluginCommand command;
        
        command = getServer().getPluginCommand("a");
        command.setExecutor(adminExec);
        
        command = getServer().getPluginCommand("me");
        command.setExecutor(meExec);
        
        command = getServer().getPluginCommand("p");
        command.setExecutor(partyExec);
        
        craftirc.unregisterEndPoint(config.adminTag);
        craftirc.unregisterEndPoint(config.gameTag);
        
        for(PartyPoint v : partyPoints.values())
            craftirc.unregisterEndPoint(v.getTag());
    }
    
    public void reload()
    {
        String party, tag;
        PartyPoint partyp;
        
        craftirc.unregisterEndPoint(config.adminTag);
        craftirc.unregisterEndPoint(config.gameTag);
        
        for(PartyPoint v : partyPoints.values())
            craftirc.unregisterEndPoint(v.getTag());
        
        config.load();
        
        if(!craftirc.registerEndPoint(config.adminTag, adminPoint))
            Log.severe("Unable to register CraftIRC tag: %s", config.adminTag);
        
        if(!craftirc.registerEndPoint(config.gameTag, gamePoint))
            Log.severe("Unable to register CraftIRC tag: %s", config.gameTag);
        
        for(Entry<String, String> e : config.parties.entrySet()) {
            party  = e.getKey();
            tag    = e.getValue();
            partyp = new PartyPoint(this, tag, party);
            
            if(craftirc.registerEndPoint(tag, partyp))
                partyPoints.put(party, partyp);
            else
                Log.severe("Unable to register CraftIRC tag: %s", tag);
        }
    }
    
    private RelayedMessage fillMessage(RelayedMessage rmsg,
                                       CommandSender sender, String msg)
    {
        Player p;
        
        if(sender instanceof Player) {
            p = (Player) sender;
            
            rmsg.setField("realSender", p.getName());
            rmsg.setField("sender",     p.getDisplayName());
            rmsg.setField("world",      p.getWorld().getName());
        } else {
            rmsg.setField("sender",     sender.getName());
        }
        
        rmsg.setField("message", msg);
        return rmsg;
    }
    
    /**
     * Test if a CommandSender has a specific permission
     * 
     * @param sender  A CommandSender
     * @param perm    A String containing the permission node
     * 
     * @return        TRUE if the player has permission,
     *                otherwise FALSE
     **/
    public boolean hasPermission(CommandSender sender, String perm)
    {
        if(!(sender instanceof Player))
            return true;
        
        return ((Player) sender).hasPermission(perm);
    }
    
    /**
     * Test if a CommandSender has a specific permission.  If the
     * permission does not exist, the player will receive a message
     * informing them of their insufficient permissions. 
     * 
     * @param sender  A CommandSender
     * @param perm    A String containing the permission node
     * 
     * @return        TRUE if the player has permission,
     *                otherwise FALSE
     **/
    public boolean hasPermissionM(CommandSender sender, String perm)
    {
        if(hasPermission(sender, perm))
            return true;
        
        Message.severe(sender, "You don't have permission for that");
        return false;
    }
    
    /**
     * Send a message to admin chat in game
     * 
     * @param rmsg  The RelayedMessage to send
     **/
    public void adminMessageToGame(RelayedMessage rmsg)
    {
        String msg;
        
        msg = rmsg.getMessage(adminPoint);
        
        for(Player p : getServer().getOnlinePlayers()) {
            if(mcPermissions.getInstance().adminChat(p) || p.isOp())
                p.sendMessage(msg);
        }
    }
    
    /**
     * Send a message to admin chat in game
     * 
     * @param sender  A CommandSender
     * @param event   A String containing a CraftIRC event
     * @param msg     The message to send
     **/
    public void adminMessageToGame(CommandSender sender,
                                   String event, String msg)
    {
        RelayedMessage rmsg;
        
        rmsg = craftirc.newMsg(gamePoint, adminPoint, event);
        rmsg = fillMessage(rmsg, sender, msg);
        
        adminMessageToGame(rmsg);
    }
    
    /**
     * Send a message to admin channel tag
     * 
     * @param sender  A CommandSender
     * @param event   A String containing a CraftIRC event
     * @param msg     The message to send
     **/
    public void adminMessageToIRC(CommandSender sender,
                                  String event, String msg)
    {
        RelayedMessage rmsg;
        
        rmsg = craftirc.newMsg(adminPoint, null, event);
        rmsg = fillMessage(rmsg, sender, msg);
        
        rmsg.post();
    }
    
    /**
     * Send a message to a party chat in game
     * 
     * @param rmsg   The RelayedMessage to send
     * @param party  A String containing the party name
     **/
    public void partyMessageToGame(RelayedMessage rmsg, String party)
    {
        PlayerProfile pp;
        PartyPoint pep;
        String msg;
        
        pep = partyPoints.get(party);
        
        if(pep == null)
            return;
        
        rmsg.setField("srcParty", party);
        msg = rmsg.getMessage(pep);
        
        for(Player p : getServer().getOnlinePlayers()) {
            pp = Users.getProfile(p);
            
            if(party.equals(pp.getParty()))
                p.sendMessage(msg);
        }
    }
    
    /**
     * Send a message to a party chat in game
     * 
     * @param sender  A CommandSender
     * @param event   A String containing a CraftIRC event
     * @param party   A String containing the party name
     * @param msg     The message to send
     **/
    public void partyMessageToGame(CommandSender sender, String event,
                                   String party, String msg)
    {
        RelayedMessage rmsg;
        PartyPoint pep;
        
        pep = partyPoints.get(party);
        
        if(pep == null)
            return;
        
        rmsg = craftirc.newMsg(gamePoint, pep, event);
        rmsg = fillMessage(rmsg, sender, msg);
        
        partyMessageToGame(rmsg, party);
    }
    
    /**
     * Send a message to a party channel 
     * 
     * @param sender  A CommandSender
     * @param event   A String containing a CraftIRC event
     * @param party   A String containing the party name
     * @param msg     The message to send
     **/
    public void partyMessageToIRC(CommandSender sender, String event,
                                  String party, String msg)
    {
        RelayedMessage rmsg;
        PartyPoint pep;
        
        pep = partyPoints.get(party);
        
        if(pep == null)
            return;
        
        rmsg = craftirc.newMsg(pep, null, event);
        rmsg = fillMessage(rmsg, sender, msg);
        
        rmsg.setField("srcParty", party);
        rmsg.post();
    }
}
