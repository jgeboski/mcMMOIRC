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

package org.jgeboski.mcmmoirc.point;

import java.util.List;

import com.ensifera.animosity.craftirc.EndPoint;
import com.ensifera.animosity.craftirc.RelayedMessage;

import org.jgeboski.mcmmoirc.mcMMOIRC;

public class GamePoint implements EndPoint
{
    protected mcMMOIRC mirc;

    public GamePoint(mcMMOIRC mirc)
    {
        this.mirc = mirc;
    }

    public Type getType()
    {
        return Type.MINECRAFT;
    }

    public void messageIn(RelayedMessage msg)
    {

    }

    public boolean userMessageIn(String username, RelayedMessage msg)
    {
        return false;
    }

    public boolean adminMessageIn(RelayedMessage msg)
    {
        return false;
    }

    public List<String> listUsers()
    {
        return null;
    }

    public List<String> listDisplayUsers()
    {
        return null;
    }
}
