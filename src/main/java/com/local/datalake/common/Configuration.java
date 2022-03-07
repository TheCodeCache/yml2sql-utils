package com.local.datalake.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class to load any properties file,
 * 
 * by default, it loads /config.properties file
 */
public class Configuration {

    private static Properties props = null;

    /**
     * Loads properties
     * 
     * @param filePath
     * @return
     */
    public static Properties loadProperties(String filePath) {
        Properties prop = new Properties();
        InputStream in = Configuration.class.getResourceAsStream(filePath);
        try {
            prop.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

    /**
     * Default - loads properties
     * 
     * @return
     */
    public static Properties loadProperties() {
        return props != null ? props : (props = loadProperties("/config.properties"));
    }
}
