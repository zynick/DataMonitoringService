package com.zynick.commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration Reader
 * @author zynick
 */
public class Configuration {

    private Map<String, String> map = new HashMap<String, String> ();

    /**
     * Read configuration from path and store key value pair in map
     * @param path
     * @throws IOException
     */
    public Configuration(String path) throws IOException {
        // provide absolute directory if hit exception
        File f = new File(path);
        if (!f.exists())
            throw new FileNotFoundException(f.getAbsolutePath() + " does not exist.");
        
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String line = null;
        int i = -1;
        while ((line = reader.readLine()) != null) {
            
            // ignore empty line and comments
            if (line.length() == 0 || line.startsWith("#"))
                continue;
            
            // ignore if '=' not found
            if ((i = line.indexOf('=')) > 0) {
                String key = line.substring(0, i).trim();
                String value = line.substring(i + 1, line.length()).trim();
                map.put(key, value);
            }
        }
        reader.close();
    }

    public String get(String key) {
        return map.get(key);
    }
    
    public Map<String, String> getMap() {
        return map;
    }

}
