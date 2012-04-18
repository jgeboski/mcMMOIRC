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

import org.bukkit.configuration.file.YamlConfiguration;

public class Configuration extends YamlConfiguration
{
    private File file;
    
    public boolean adminLog;
    public boolean partyLog;
    
    public String adminTag;
    public String defaultTag;
    public String partyTag;
    
    public HashMap<String, String> parties;
    
    public Configuration(File file)
    {
        this.file = file;
        
        adminLog = true;
        partyLog = true;
        
        adminTag   = "adminchat";
        defaultTag = "default";
        partyTag   = "partychat";
        
        parties = new HashMap<String, String>();
    }
    
    public void load()
    {
        String party, tag;
        
        try {
            super.load(file);
        } catch(Exception e) {
            Log.warning("Unable to load: %s", file.toString());
        }
        
        adminLog = getBoolean("logging.admin", adminLog);
        partyLog = getBoolean("logging.party", partyLog);
        
        adminTag   = getString("tags.admin",   adminTag);
        defaultTag = getString("tags.default", defaultTag);
        partyTag   = getString("tags.party",   partyTag);
        
        for(Map<?, ?> m : getMapList("parties")) {
            party = (String) m.get("name");
            tag   = (String) m.get("tag");
            
            if((party == null) || (tag == null))
                continue;
            
            parties.put(party, tag);
        }
        
        if(!file.exists())
            save();
    }
    
    public void save()
    {
        ArrayList<Map<String, String>> cparties;
        Map<String, String> cparty;
        
        set("logging.admin", adminLog);
        set("logging.party", partyLog);
        
        set("tags.admin",   adminTag);
        set("tags.default", defaultTag);
        set("tags.party",   partyTag);
        
        cparties = new ArrayList<Map<String, String>>();
        
        for(Entry<String, String> e : parties.entrySet()) {
            cparty = new HashMap<String, String>();
            cparty.put("name", e.getKey());
            cparty.put("tag",  e.getValue());
            
            cparties.add(cparty);
        }
        
        set("parties", cparties);
        
        try {
            super.save(file);
        } catch(Exception e) {
            Log.warning("Unable to save: %s", file.toString());
        }
    }
}
