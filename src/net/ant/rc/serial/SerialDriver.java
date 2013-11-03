package net.ant.rc.serial;

import net.ant.rc.serial.exception.CommPortException;

import java.util.Date;

/**Base Driver-abstraction class.
 * Unification of hardware types for your application layer.
 * SerialDriver translates app commands to hardware languages.
 * Extend it according to your application.
 * Use {@link SerialHardwareDetector HardwareDetector} to choose the appropriate hardware extension at runtime.
 * <img alt="SerialDriver functional diagram" src="https://raw.github.com/ant2012/SerialDriver/master/SerialDriverArchitecture.png" />
 * @author Ant
 * @version 1.0
 */
public abstract class SerialDriver {
    protected final SerialCommunicator serialCommunicator;
    private final SerialConnection serialConnection;
    private long sensorLastTime = 0;
    private final Battery battery;
    private int lastTemperature = 0;
    private final int SENSOR_REFRESH_PERIOD;
    private final Config config;


    /**Sends Vector-style command to hardware.
     * Parameters x,y - is shift vector of single joystick. To use 2 joysticks see
     * {@link #sendTractorCommand sendTractorCommand} method
     * @param x X joystick shift
     * @param y Y joystick shift
     * @see VectorCommand
     * @return Firmware answer text
     */
    public abstract String sendVectorCommand(int x, int y) throws CommPortException;

    /**Sends Tractor-style command to hardware.
     * @param left Left joystick speed (Positive values means straight direction. Negatives - forward.)
     * @param right Right joystick speed
     * @see TractorCommand
     * @return Firmware answer text
     */
    public abstract String sendTractorCommand(int left, int right) throws CommPortException;

    /**Method for constructing programmatic (not analog like WebRC) moves.
     * This method isn't currently used in any example.
     * But it gives direct access to hardware motors for your application without translation by SerialDriver.
     * @param eachWheelCommand Defines speeds for each motor of your device
     * @return Firmware answer text
     */
    public abstract String sendEachWheelCommand(EachWheelCommand eachWheelCommand) throws CommPortException;

    /**Not useful from the application.
     * Initialise {@link SerialHardwareDetector HardwareDetector}
     * and use {@link SerialHardwareDetector#getSerialDriver() detector.getSerialDriver()} method to access a SerialDriver instance
     */
    protected SerialDriver(SerialConnection serialConnection, Config config) {
        this.serialConnection = serialConnection;
        this.serialCommunicator = serialConnection.getSerialCommunicator();
        this.battery = new Battery(config);
        this.config = config;
        SENSOR_REFRESH_PERIOD = Integer.parseInt(config.getOption(Config.SENSOR_REFRESH_PERIOD));
    }

    /**Disconnects hardware.
     * @see net.ant.rc.serial.SerialConnection#disconnect()
     */
    public void disconnect(){
        this.serialConnection.disconnect();
    }

    /**Gets voltage level and temperature from hardware chip sensors.
     * Use it to periodically refresh indicators.
     * And use indicator getters to access values at any time.
     * @see #getChipVoltage()
     * @see #getChipTemperature()
     */
    public final void getChipParameters() throws CommPortException {
        long timestamp = (new Date()).getTime();
        if((timestamp - sensorLastTime) > SENSOR_REFRESH_PERIOD){
            sensorLastTime = timestamp;
            battery.setVoltage(Integer.parseInt(this.serialCommunicator.sendCommand("voltage")));
            lastTemperature = Integer.parseInt(this.serialCommunicator.sendCommand("temperature"));
        }
    }

    /**@return Voltage indicator value in milli Volts.
     * @see #getChipParameters()
     * @see Battery
     */
    public final int getChipVoltage() {
        return battery.getCurrentVoltage();
    }

    /**@return Temperature indicator value in milli Celsius.
     * @see #getChipParameters()
     */
    public final int getChipTemperature() {
        return lastTemperature;
    }

    public SerialConnection getSerialConnection() {
        return serialConnection;
    }

    public Config getConfig() {
        return config;
    }

    public Battery getBattery() {
        return battery;
    }
}
