package net.ant.rc.serial.exception;

/**On unknown hardware version received from device
 * @author Ant
 * @version 1.0
 */
public class UnsupportedHardwareException extends Exception{
    public UnsupportedHardwareException(String message){
        super(message);
    }
}
