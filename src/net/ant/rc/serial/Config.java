package net.ant.rc.serial;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Properties;
import java.util.Vector;

/**
 * <Put class description here>
 *
 * @author Ant
 * @version 1.0
 */
public class Config {
    private Logger logger = Logger.getLogger(this.getClass());

    private Properties properties = new Properties();
    private final Properties defaultProperties;
    private boolean propertiesExists = false;
    private final String FILE_NAME = "SerialDriver.conf";
    private String fileName;
    private final File file;
    private final Vector<String> allKeys;

    private Properties initDefault(){
        Properties p = new Properties();
        p.setProperty("CommPortName", "COM3");
        //add properties here
        return p;
    }

    public Config(String workPath) {
        defaultProperties = initDefault();
        allKeys = new Vector<String>();
        allKeys.addAll(defaultProperties.stringPropertyNames());

        fileName = workPath + FILE_NAME;
        file = new File(fileName);

        boolean loaded = getSavedConfig();
        if(!loaded || !allKeysExists()){
            writeDefaultConfig();
        }
    }

    public void setOption(String key, String val, String comments){
        String old = properties.getProperty(key);
        properties.setProperty(key, val);
        logger.info("Config changed \"" + key + "\". Old=" + old + "; New=" + val);
        saveConfig(comments);
    }

    public String getOption(String key){
        return properties.getProperty(key);
    }

    private boolean getSavedConfig(){
        boolean configLoaded = false;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            properties.load(fileInputStream);
            configLoaded = true;
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            logger.warn("File \"" + fileName + "\" not found");
        } catch (IOException e) {
            if(configLoaded){
                logger.warn("Can not close configuration file \"" + fileName + "\"");
            }else{
                logger.warn("Can not load configuration from file \"" + fileName + "\"");
            }
        }
        return configLoaded;
    }

    private void writeDefaultConfig(){
        logger.info("Loading Default configuration..");
        properties = defaultProperties;
        saveConfig("Default config. Created by " + this.getClass());
    }

    private void saveConfig(String comments){
        logger.info("Saving configuration to file..");
        try {
            FileOutputStream out = new FileOutputStream(file);
            properties.store(out, comments);
            out.close();
        } catch (FileNotFoundException e) {
            logger.error("Can not create configuration file \"" + fileName + "\". Can not find path.", e);
        } catch (IOException e) {
            logger.error("Can not write configuration to file \"" + fileName + "\". Can not save file.", e);
        }
    }

    private boolean allKeysExists(){
        boolean result = properties.keySet().containsAll(allKeys);
        return result;
    }

}
