package net.ant.rc.serial.arduino2wd;

import net.ant.rc.serial.EachWheelCommand;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 07.02.13
 * Time: 12:04
 * To change this template use File | Settings | File Templates.
 */
public class Arduino2WDEachWheelCommand extends EachWheelCommand {
    public int leftWheelSpeed;
    public int rightWheelSpeed;

    public Arduino2WDEachWheelCommand(int leftWheelSpeed, int rightWheelSpeed) {
        super();
        this.leftWheelSpeed = leftWheelSpeed;
        this.rightWheelSpeed = rightWheelSpeed;
    }
}
