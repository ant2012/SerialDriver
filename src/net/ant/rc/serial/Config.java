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
    public static final String COMM_PORT_NAME = "CommPortName";
    public static final String BATTERY_MIN_VOLTAGE = "Battery.MinVoltage";
    public static final String BATTERY_MAX_VOLTAGE = "Battery.MaxVoltage";
    public static final String SERIAL_LISTENER_TIMEOUT = "SerialCommunicator.PortListenerTimeout";
    public static final String COMM_PORT_INTERNAL_TIMEOUT = "SerialConnection.PortInternalTimeout";
    public static final String STATE_REFRESH_PERIOD = "SerialDriver.HardwareStateRefreshPeriod";
    public static final String SERVICE_MAX_QUEUE_SIZE = "SerialService.MaxQueueSize";
    public static final String SERVICE_POLL_WAIT_TIMEOUT = "SerialService.PollWaitTimeout";
    public static final String SERVICE_RECONNECT_TIMEOUT = "SerialService.ReconnectTimeout";


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
        p.setProperty(COMM_PORT_NAME, "COM3");
        p.setProperty(BATTERY_MIN_VOLTAGE, "3000");//3V
        p.setProperty(BATTERY_MAX_VOLTAGE, "11100");//11.1V
        p.setProperty(SERIAL_LISTENER_TIMEOUT, "5000");//5s wait to get answer or to init listener at 1st time
        p.setProperty(COMM_PORT_INTERNAL_TIMEOUT, "2000");//2s initial timeout during port open method.
        p.setProperty(STATE_REFRESH_PERIOD, String.valueOf(60 * 1000));//60s period refreshing hardware sensors
        p.setProperty(SERVICE_MAX_QUEUE_SIZE, String.valueOf(20));
        p.setProperty(SERVICE_POLL_WAIT_TIMEOUT, String.valueOf(3000));
        p.setProperty(SERVICE_RECONNECT_TIMEOUT, String.valueOf(30000));

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
