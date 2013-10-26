package net.ant.rc.serial.arduino2wd;

import net.ant.rc.serial.EachWheelCommand;

/**Message class for Arduino-based 2WD wheel robot.
 * Used between SerialDriver and Robot.
 * Modify it for your Robot Firmware and use in your SerialDriver extension.
 * <img src="https://raw.github.com/ant2012/SerialDriver/master/SerialDriverArchitecture.png" />
 * @author Ant
 * @version 1.0
 * @see net.ant.rc.serial.arduino2wd.Arduino2WDSerialDriver See how we use it in Arduino2WDSerialDriver
 */
public class Arduino2WDEachWheelCommand extends EachWheelCommand {
    public int leftWheelSpeed;
    public int rightWheelSpeed;

    /**
     * @param leftWheelSpeed  Speed for left  robot wheel (sensitivity is not normalised)
     * @param rightWheelSpeed Speed for right robot wheel (sensitivity is not normalised)
     * @see net.ant.rc.serial.arduino2wd.Arduino2WDSerialDriver See how we use it in Arduino2WDSerialDriver
     */
    public Arduino2WDEachWheelCommand(int leftWheelSpeed, int rightWheelSpeed) {
        super();
        this.leftWheelSpeed = leftWheelSpeed;
        this.rightWheelSpeed = rightWheelSpeed;
    }
}
