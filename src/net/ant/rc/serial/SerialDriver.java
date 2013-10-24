package net.ant.rc.serial;

import net.ant.rc.serial.exception.CommPortException;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 07.02.13
 * Time: 12:23
 * To change this template use File | Settings | File Templates.
 */
public abstract class SerialDriver {
    public final SerialCommunicator serialCommunicator;
    private final SerialHardwareDetector serialHardwareDetector;

    public abstract String sendVectorCommand(int x, int y) throws CommPortException;
    public abstract String sendTractorCommand(int left, int right) throws CommPortException;

    public abstract String sendEachWheelCommand(EachWheelCommand eachWheelCommand) throws CommPortException;

    private long sensorLastTime = 0;
    private Battery battery;
    private int lastTemperature = 0;
    private long SENSOR_REQUEST_TIMEOUT = 60 * 1000;

    public SerialDriver(SerialHardwareDetector serialHardwareDetector) {
        this.serialHardwareDetector = serialHardwareDetector;
        this.serialCommunicator = serialHardwareDetector.getSerialCommunicator();
        this.battery = new Battery();
    }

    public void disconnect(){
        this.serialHardwareDetector.disconnect();
    }

    public final void getChipParameters() throws CommPortException {
        long timestamp = (new Date()).getTime();
        if((timestamp - sensorLastTime) > SENSOR_REQUEST_TIMEOUT){
            sensorLastTime = timestamp;
            battery.setVoltage(Integer.parseInt(this.serialCommunicator.sendCommand("voltage")));
            lastTemperature = Integer.parseInt(this.serialCommunicator.sendCommand("temperature"));
        }
    }

    public final int getChipVoltage() {
        return battery.getCurrentVoltage();
    }

    public final int getChipTemperature() {
        return lastTemperature;
    }

}
