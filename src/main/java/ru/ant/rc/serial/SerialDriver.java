package ru.ant.rc.serial;

import ru.ant.rc.serial.exception.CommPortException;

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
    private final Config config;
    private ArduinoState arduinoState;


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
        this.config = config;
    }

    /**Disconnects hardware.
     * @see SerialConnection#disconnect()
     */
    public void disconnect(){
        this.serialConnection.disconnect();
    }

    public ArduinoState getArduinoState() throws CommPortException {
        if(arduinoState == null) arduinoState = new ArduinoState(serialCommunicator, config);
        return arduinoState;
    }

    public SerialConnection getSerialConnection() {
        return serialConnection;
    }

    public Config getConfig() {
        return config;
    }

    public void pause(){
        serialConnection.pause();
    }

    public void resume(){
        serialConnection.resume();
    }
}
