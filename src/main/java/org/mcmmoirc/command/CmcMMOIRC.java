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

package org.mcmmoirc.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.mcmmoirc.mcMMOIRC;
import org.mcmmoirc.Message;

public class CmcMMOIRC implements CommandExecutor
{
    protected mcMMOIRC mirc;
    
    public CmcMMOIRC(mcMMOIRC mirc)
    {
        this.mirc = mirc;
    }
    
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args)
    {
        String cmd;
        
        if(!mirc.hasPermissionM(sender, "mcmmoirc.reload"))
            return true;
        
        if(args.length < 1) {
            Message.info(sender, command.getUsage());
            return true;
        }
        
        cmd = args[0].toLowerCase();
        
        if(cmd.matches("reload|rel"))
            reload(sender);
        else
            Message.info(sender, command.getUsage());
        
        return true;
    }
    
    private void reload(CommandSender sender)
    {
        mirc.reload();
        Message.info(sender, "mcMMOIRC successfully reloaded");
    }
}