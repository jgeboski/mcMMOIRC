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
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
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

import com.gmail.nossr50.mcPermissions;

import org.mcmmoirc.command.CA;
import org.mcmmoirc.command.CmcMMOIRC;
import org.mcmmoirc.point.AdminPoint;
import org.mcmmoirc.point.GamePoint;

public class mcMMOIRC extends JavaPlugin
{
    public static String pluginName = "mcMMOIRC";
    
    public Configuration config;
    public CraftIRC craftirc;
    public CommandExecutor adminExec;
    
    public AdminPoint adminPoint;
    public GamePoint gamePoint;
    
    private EventListener events;
    
    public void onLoad()
    {
        config     = new Configuration(new File(getDataFolder(), "config.yml"));
        events     = new EventListener(this);
        adminPoint = new AdminPoint(this);
        gamePoint  = new GamePoint();
    }
    
    public void onEnable()
    {
        PluginCommand command;
        PluginManager pm;
        Plugin plugin;
        
        config.load();
        
        pm     = getServer().getPluginManager();
        plugin = pm.getPlugin("CraftIRC");
        
        if(plugin == null) {
            Log.severe("Unable to find CraftIRC");
            return;
        }
        
        craftirc  = (CraftIRC) plugin;
        command   = getServer().getPluginCommand("a");
        adminExec = command.getExecutor();
        
        if(!craftirc.registerEndPoint(config.adminTag, adminPoint))
            Log.severe("Unable to register CraftIRC tag: %s", config.adminTag);
        
        if(!craftirc.registerEndPoint(config.gameTag, gamePoint))
            Log.severe("Unable to register CraftIRC tag: %s", config.gameTag);
        
        command.setExecutor(new CA(this));
        getCommand("mcmmoirc").setExecutor(new CmcMMOIRC(this));
        
        events.register();
    }
    
    public void onDisable()
    {
        PluginCommand command;
        
        command = getServer().getPluginCommand("a");
        command.setExecutor(adminExec);
    }
    
    public void reload()
    {
        craftirc.unregisterEndPoint(config.adminTag);
        craftirc.unregisterEndPoint(config.gameTag);
        
        config.load();
        
        if(!craftirc.registerEndPoint(config.adminTag, adminPoint))
            Log.severe("Unable to register CraftIRC tag: %s", config.adminTag);
        
        if(!craftirc.registerEndPoint(config.gameTag, gamePoint))
            Log.severe("Unable to register CraftIRC tag: %s", config.gameTag);
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
        
        Log.info("[mcMMOIRC] Game: %s", rmsg.getMessage(adminPoint));
    }
    
    /**
     * Send a message to admin chat in game
     * 
     * @param sender  A CommandSender
     * @param target  A String containing a CraftIRC target
     * @param msg     The message to send
     **/
    public void adminMessageToGame(CommandSender sender,
                                   String target, String msg)
    {
        RelayedMessage rmsg;
        Player p;
        
        rmsg = craftirc.newMsg(gamePoint, adminPoint, "chat");
        
        if(sender instanceof Player) {
            p = (Player) sender;
            
            rmsg.setField("realSender", p.getName());
            rmsg.setField("sender",     p.getDisplayName());
            rmsg.setField("world",      p.getWorld().getName());
        } else {
            rmsg.setField("sender",     sender.getName());
        }
        
        rmsg.setField("message", msg);
        adminMessageToGame(rmsg);
    }
    
    /**
     * Send a message to admin channel tag
     * 
     * @param sender  A CommandSender
     * @param msg     The message to send
     **/
    public void adminMessageToIRC(CommandSender sender, String msg)
    {
        RelayedMessage rmsg;
        Player p;
        
        rmsg = craftirc.newMsg(adminPoint, null, "chat");
        
        if(sender instanceof Player) {
            p = (Player) sender;
            
            if(!mcPermissions.getInstance().adminChat(p) && !p.isOp())
                return;
            
            rmsg.setField("realSender", p.getName());
            rmsg.setField("sender",     p.getDisplayName());
            rmsg.setField("world",      p.getWorld().getName());
        } else {
            rmsg.setField("sender",     sender.getName());
        }
        
        rmsg.setField("message", msg);
        rmsg.post();
    }
}
