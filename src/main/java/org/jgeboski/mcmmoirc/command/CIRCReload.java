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

package org.jgeboski.mcmmoirc.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.jgeboski.mcmmoirc.mcMMOIRC;
import org.jgeboski.mcmmoirc.util.Message;
import org.jgeboski.mcmmoirc.util.Utils;

public class CIRCReload implements CommandExecutor
{
    protected mcMMOIRC mirc;

    public CIRCReload(mcMMOIRC mirc)
    {
        this.mirc = mirc;
    }

    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args)
    {
        String cmd;

        if (!Utils.hasPermission(sender, "craftirc.ircreload"))
            return true;

        mirc.ircrExec.onCommand(sender, command, label, args);
        mirc.reload();

        Message.info(sender, "Successfully reloaded %s",
                     mirc.getDescription().getFullName());

        return true;
    }
}
