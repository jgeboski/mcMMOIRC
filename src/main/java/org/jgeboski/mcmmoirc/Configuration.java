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

package org.jgeboski.mcmmoirc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import org.jgeboski.mcmmoirc.util.Log;

public class Configuration extends YamlConfiguration
{
    private File file;

    public String adminTag;

    public String adminPrefix;
    public String adminSuffix;

    public HashMap<String, Party> parties;

    public Configuration(File file)
    {
        this.file = file;

        adminTag = "adminchat";

        adminPrefix = null;
        adminSuffix = null;

        parties = new HashMap<String, Party>();
    }

    public void load()
    {
        ConfigurationSection cs;
        String name;
        String tag;
        String prefix;
        String suffix;

        try {
            super.load(file);
        } catch (Exception e) {
            Log.warning("Unable to load: %s", file.toString());
            e.printStackTrace();
        }

        cs          = getConfigurationSection("admin");
        adminTag    = cs.getString("tag",    null);
        adminPrefix = cs.getString("prefix", adminPrefix);
        adminSuffix = cs.getString("suffix", adminSuffix);

        if (adminPrefix != null)
            adminPrefix = ChatColor.translateAlternateColorCodes('&', adminPrefix);

        if (adminSuffix != null)
            adminSuffix = ChatColor.translateAlternateColorCodes('&', adminSuffix);

        for (Map<?, ?> m : getMapList("parties")) {
            name   = (String) m.get("name");
            tag    = (String) m.get("tag");
            prefix = (String) m.get("prefix");
            suffix = (String) m.get("suffix");

            if ((name == null) || (tag == null))
                continue;

            if (prefix != null)
                prefix = ChatColor.translateAlternateColorCodes('&', prefix);

            if (suffix != null)
                suffix = ChatColor.translateAlternateColorCodes('&', suffix);

            parties.put(name, new Party(name, tag, prefix, suffix));
        }

        if (!file.exists()) {
            save();
            return;
        }


        /* Backwards compatibility for configuration values */

        if (adminTag != null)
            return;

        adminTag = getString("tags.admin", adminTag);

        if (adminTag == null) {
            adminTag = "adminchat";
            return;
        }

        set("tags", null);
        save();
    }

    public void save()
    {
        ArrayList<Map<String, String>> cps;
        Map<String, String>            cp;

        ConfigurationSection cs;
        String s;
        Party  p;

        cs = getConfigurationSection("admin");
        cs.set("tag", adminTag);

        s = adminPrefix;
        s = (s != null) ? s.replace(ChatColor.COLOR_CHAR, '&') : null;
        cs.set("prefix", s);

        s = adminSuffix;
        s = (s != null) ? s.replace(ChatColor.COLOR_CHAR, '&') : null;
        cs.set("suffix", s);

        cps = new ArrayList<Map<String, String>>();

        for (Entry<String, Party> e : parties.entrySet()) {
            cp = new HashMap<String, String>();
            p  = e.getValue();

            cp.put("name", p.name);
            cp.put("tag",  p.tag);

            s = p.prefix;
            s = (s != null) ? s.replace(ChatColor.COLOR_CHAR, '&') : null;
            cp.put("prefix", s);

            s = p.suffix;
            s = (s != null) ? s.replace(ChatColor.COLOR_CHAR, '&') : null;
            cp.put("suffix", s);

            cps.add(cp);
        }

        set("parties", cps);

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
