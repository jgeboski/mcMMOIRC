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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import org.mcmmoirc.util.Log;

public class Configuration extends YamlConfiguration
{
    private File file;

    public String adminTag;
    public String partyTag;

    public HashMap<String, String> parties;

    public Configuration(File file)
    {
        this.file = file;

        adminTag  = "adminchat";
        partyTag  = "partychat";

        parties   = new HashMap<String, String>();
    }

    public void load()
    {
        ConfigurationSection cs;
        String party;
        String tag;

        try {
            super.load(file);
        } catch (Exception e) {
            Log.warning("Unable to load: %s", file.toString());
        }

        cs       = getConfigurationSection("tags");
        adminTag = cs.getString("admin", adminTag);
        partyTag = cs.getString("party", partyTag);

        for (Map<?, ?> m : getMapList("parties")) {
            party = (String) m.get("name");
            tag   = (String) m.get("tag");

            if ((party != null) && (tag != null))
                parties.put(party, tag);
        }

        if (!file.exists())
            save();
    }

    public void save()
    {
        ArrayList<Map<String, String>> cparties;
        Map<String, String>            cparty;

        ConfigurationSection cs;

        cs = getConfigurationSection("tags");
        cs.set("admin", adminTag);
        cs.set("party", partyTag);

        cparties = new ArrayList<Map<String, String>>();

        for (Entry<String, String> e : parties.entrySet()) {
            cparty = new HashMap<String, String>();
            cparty.put("name", e.getKey());
            cparty.put("tag",  e.getValue());

            cparties.add(cparty);
        }

        set("parties", cparties);

        try {
            super.save(file);
        } catch (Exception e) {
            Log.warning("Unable to save: %s", file.toString());
        }
    }

    public ConfigurationSection getConfigurationSection(String path)
    {
        ConfigurationSection ret;

        ret = super.getConfigurationSection(path);

        if (ret == null)
            ret = createSection(path);

        return ret;
    }
}
