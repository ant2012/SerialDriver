package ru.ant.rc.serial;

import org.apache.log4j.Logger;
import ru.ant.common.App;

import java.io.IOException;

/**
 * <Put class description here>
 *
 * @author Ant
 * @version 1.0
 */
public class Config {
    private Logger log = Logger.getLogger(getClass());
    public static final String FILE_NAME = "serial-driver.properties";

    public static final String COMM_PORT_NAME = "CommPortName";
    public static final String BATTERY_MIN_VOLTAGE = "Battery.MinVoltage";
    public static final String BATTERY_MAX_VOLTAGE = "Battery.MaxVoltage";
    public static final String BATTERY_MIN_VOLTAGE_CALIBRATION = "Battery.MinVoltage.calibration";
    public static final String BATTERY_MAX_VOLTAGE_CALIBRATION = "Battery.MaxVoltage.calibration";
    public static final String SERIAL_LISTENER_TIMEOUT = "SerialCommunicator.PortListenerTimeout";
    public static final String COMM_PORT_INTERNAL_TIMEOUT = "SerialConnection.PortInternalTimeout";
    public static final String STATE_REFRESH_PERIOD = "SerialDriver.HardwareStateRefreshPeriod";
    public static final String SERVICE_MAX_QUEUE_SIZE = "SerialService.MaxQueueSize";
    public static final String SERVICE_POLL_WAIT_TIMEOUT = "SerialService.PollWaitTimeout";
    public static final String SERVICE_RECONNECT_TIMEOUT = "SerialService.ReconnectTimeout";
    public static final String SLEEP_TIMEOUT = "SerialService.SleepTimeout";

    public void setOption(String key, String val, String comments){
        String old = App.getProperty(key);
        App.setProperty(FILE_NAME, key, val);
        log.info("Config changed \"" + key + "\". Old=" + old + "; New=" + val);
        saveConfig(comments);
    }

    public String getOption(String key){
        return App.getProperty(key);
    }

    private void saveConfig(String comments){
        log.info("Saving configuration to file..");
        try {
            App.saveProperties(FILE_NAME, comments);
        } catch (IOException e) {
            log.error("Can not write configuration to file \"" + FILE_NAME, e);
        }
    }

}
