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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.nossr50.mcPermissions;

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
        Player p;
        int i;
        
        if(args.length < 1) {
            mirc.adminExec.onCommand(sender, command, label, new String[0]);
            return true;
        }
        
        if(sender instanceof Player) {
            p = (Player) sender;
            
            if(!mcPermissions.getInstance().adminChat(p) && !p.isOp())
                return true;
        }
        
        for(msg = args[0], i = 1; i < args.length; i++)
            msg = msg.concat(" " + args[i]);
        
        mirc.adminMessageToGame(sender, "chat", msg);
        mirc.adminMessageToIRC(sender, msg);
        return true;
    }
}
