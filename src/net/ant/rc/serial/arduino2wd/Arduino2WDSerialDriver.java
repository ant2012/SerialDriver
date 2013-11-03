package net.ant.rc.serial.arduino2wd;

import net.ant.rc.serial.*;
import net.ant.rc.serial.exception.CommPortException;

/**SerialDriver extension for Arduino-based 2WD wheel robot.
 * Used for translate commands from your application to your hardware (firmware logic).
 * Modify it for your Robot Firmware.
 * <img alt="SerialDriver functional diagram" src="https://raw.github.com/ant2012/SerialDriver/master/SerialDriverArchitecture.png" />
 * @author Ant
 * @version 1.0
 * @see net.ant.rc.serial.arduino2wd.Arduino2WDEachWheelCommand Don't forget to modify Arduino2WDEachWheelCommand for your hardware too.
 */
public class Arduino2WDSerialDriver extends SerialDriver {

    private final int maxClientValue = 100;
    private final int maxSpeed = 255;

    /**Not useful from the application.
     * Initialise {@link SerialHardwareDetector HardwareDetector}
     * and use {@link SerialHardwareDetector#getSerialDriver() detector.getSerialDriver()} method to access a SerialDriver instance
     */
    public Arduino2WDSerialDriver(SerialConnection serialConnection, Config config) {
        super(serialConnection, config);
    }

    @Override
    public String sendVectorCommand(int x, int y) throws CommPortException {
        String command = generateDigitalCommand(x, -y);
        return this.serialCommunicator.sendCommand(command);
    }

    @Override
    public String sendTractorCommand(int left, int right) throws CommPortException {
        int leftSign  = (left <0)?-1:1;
        int rightSign = (right<0)?-1:1;
        int leftWheelSpeed  = (Math.abs(left) >=this.maxClientValue)?leftSign *this.maxSpeed:left *this.maxSpeed/this.maxClientValue;
        int rightWheelSpeed = (Math.abs(right)>=this.maxClientValue)?rightSign*this.maxSpeed:right*this.maxSpeed/this.maxClientValue;
        return this.serialCommunicator.sendCommand(generateDigitalCommand(new Arduino2WDEachWheelCommand(-leftWheelSpeed, -rightWheelSpeed)));
    }

    @Override
    public String sendEachWheelCommand(EachWheelCommand eachWheelCommand) throws CommPortException {
        Arduino2WDEachWheelCommand arduino2WDEachWheelCommand = (Arduino2WDEachWheelCommand)eachWheelCommand;
        return this.serialCommunicator.sendCommand(generateDigitalCommand(arduino2WDEachWheelCommand));
    }

    private String generateDigitalCommand(Arduino2WDEachWheelCommand arduino2WDEachWheelCommand) {
        return "Digital:" + arduino2WDEachWheelCommand.leftWheelSpeed + "," + arduino2WDEachWheelCommand.rightWheelSpeed;
    }

    private String generateDigitalCommand(int x, int y){

        //Save max values - it is self adaptation
        //if(Math.abs(x) > this.maxClientValue)this.maxClientValue = Math.abs(x);
        //if(Math.abs(y) > this.maxClientValue)this.maxClientValue = Math.abs(y);
        int c = (int) Math.round(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)));
        //if(c > this.maxSpeed)this.maxSpeed = c;

        //Normalize speeds to range 0..255 using max value
        c = (c>=this.maxClientValue)?this.maxSpeed:this.maxSpeed * c / this.maxClientValue;
        //x = (this.maxClientValue<=x)?this.maxSpeed:this.maxSpeed * x / this.maxClientValue;
        int sign = (y<0)?-1:1;


        y = (y==0)?0:this.maxSpeed/(Math.abs(x/y)+1);

        y = (this.maxSpeed > y) ? y : this.maxSpeed;
        y = sign * y;

        //Set direction sign
        c = c * sign;

        //2WD transform from joystick Vector to wheel's speed
        int  leftWheelSpeed = 0;
        int rightWheelSpeed = 0;

        // (I) & (IV) quadrants
        if (x >= 0) {
            leftWheelSpeed = c;
            rightWheelSpeed = y;
        }
        // (II) & (III) quadrants
        if (x < 0) {
            rightWheelSpeed = c;
            leftWheelSpeed = y;
        }
        //Format is "Digital:leftWheelSpeed,rightWheelSpeed"
        return generateDigitalCommand(new Arduino2WDEachWheelCommand(leftWheelSpeed, rightWheelSpeed));
    }
}
