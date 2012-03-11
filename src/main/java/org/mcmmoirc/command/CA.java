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

public class CA implements CommandExecutor
{
    protected mcMMOIRC mirc;
    
    public CA(mcMMOIRC mirc)
    {
        this.mirc = mirc;
    }
    
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args)
    {
        String msg;
        int i;
        
        mirc.mexecutor.onCommand(sender, command, label, args);
        
        if(args.length < 1)
            return true;
        
        msg = new String();
        for(i = 0; i < args.length; i++) {
            msg += args[i];
            
            if(i != (args.length - 1))
                msg += " ";
        }
        
        mirc.adminMessageToIRC(sender, msg);
        return true;
    }
}
