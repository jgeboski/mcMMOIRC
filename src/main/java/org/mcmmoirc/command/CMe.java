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

import com.gmail.nossr50.datatypes.PlayerProfile;
import com.gmail.nossr50.Users;

import org.mcmmoirc.mcMMOIRC;

public class CMe implements CommandExecutor
{
    protected mcMMOIRC mirc;
    
    public CMe(mcMMOIRC mirc)
    {
        this.mirc = mirc;
    }
    
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args)
    {
        PlayerProfile pp;
        Player p;
        
        String msg, party;
        int i;
        
        if(!(sender instanceof Player))
            return true;
        
        p  = (Player) sender;
        pp = Users.getProfile(p);
        
        for(msg = args[0], i = 1; i < args.length; i++)
            msg = msg.concat(" " + args[i]);
        
        if(pp.getPartyChatMode()) {
            party = pp.getParty();
            
            if(mirc.partyPoints.containsKey(party)) {
                mirc.partyMessageToGame(sender, "action", party, msg);
                mirc.partyMessageToIRC(sender, "action", party, msg);
                return true;
            }
        } else if(pp.getAdminChatMode()) {
            mirc.adminMessageToGame(sender, "action", msg);
            mirc.adminMessageToIRC(sender, "action", msg);
            return true;
        }
        
        mirc.meExec.onCommand(sender, command, label, args);
        return true;
    }
}
