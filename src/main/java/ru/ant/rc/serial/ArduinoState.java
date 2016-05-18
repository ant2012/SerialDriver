package ru.ant.rc.serial;

import org.apache.log4j.Logger;
import ru.ant.rc.serial.exception.CommPortException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 04.11.13
 * Time: 22:46
 * To change this template use File | Settings | File Templates.
 */
public class ArduinoState {
    private final int REFRESH_PERIOD;
    private long refreshLastTime = 0;

    private final Battery battery;
    private final String firmwareVersion;
    private final String hardwareType;
    private int temperature;
    private int freeRAM;
    private final int totalRAM;
    private final int sketchSize;
    private final int totalFlash;
    private final String buildGccVersion;
    private final String buildLibcVersion;
    private final String buildSourceName;
    private final Date buildDate;
    private int upTime;
    private final String buildCpuTarget;
    private final SerialCommunicator serialCommunicator;

    public ArduinoState(SerialCommunicator serialCommunicator, Config config) throws CommPortException {
        REFRESH_PERIOD = Integer.parseInt(config.getOption(Config.STATE_REFRESH_PERIOD));

        this.serialCommunicator = serialCommunicator;
        this.battery = new Battery(config);
        this.firmwareVersion = serialCommunicator.sendCommand("Version");
        this.hardwareType = serialCommunicator.sendCommand("Hardware");
        this.totalRAM = Integer.parseInt(serialCommunicator.sendCommand("TotalRAM"));
        this.sketchSize = Integer.parseInt(serialCommunicator.sendCommand("SketchSize"));
        this.totalFlash = Integer.parseInt(serialCommunicator.sendCommand("TotalFlash"));
        this.buildGccVersion = serialCommunicator.sendCommand("GccVersion");
        this.buildLibcVersion = serialCommunicator.sendCommand("LibcVersion");
        this.buildSourceName = serialCommunicator.sendCommand("SketchSourceName");
        Date buildDate;
        try {
            buildDate = new SimpleDateFormat("MMM dd yyyy HH:mm:ss").parse(serialCommunicator.sendCommand("CompileDate"));
        } catch (ParseException e) {
            buildDate = null;
            Logger logger = Logger.getLogger(this.getClass());
            logger.error(e.getMessage(), e);
        }
        this.buildDate = buildDate;
        this.buildCpuTarget = serialCommunicator.sendCommand("GccCpuTarget");
    }

    public void refresh() throws CommPortException {
        long timestamp = (new Date()).getTime();
        if((timestamp - refreshLastTime) > REFRESH_PERIOD){
            battery.setVoltage(Integer.parseInt(serialCommunicator.sendCommand("Voltage")));
            temperature = Integer.parseInt(serialCommunicator.sendCommand("Temperature"));
            freeRAM = Integer.parseInt(serialCommunicator.sendCommand("FreeRAM"));
            upTime = Integer.parseInt(serialCommunicator.sendCommand("UpTime"));
        }
    }

    public Battery getBattery() {
        return battery;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public String getHardwareType() {
        return hardwareType;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getFreeRAM() {
        return freeRAM;
    }

    public int getTotalRAM() {
        return totalRAM;
    }

    public int getSketchSize() {
        return sketchSize;
    }

    public int getTotalFlash() {
        return totalFlash;
    }

    public String getBuildGccVersion() {
        return buildGccVersion;
    }

    public String getBuildLibcVersion() {
        return buildLibcVersion;
    }

    public String getBuildSourceName() {
        return buildSourceName;
    }

    public Date getBuildDate() {
        return buildDate;
    }

    public int getUpTime() {
        return upTime;
    }

    public String getBuildCpuTarget() {
        return buildCpuTarget;
    }
}
