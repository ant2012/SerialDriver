package ru.ant.rc.serial;

import org.apache.log4j.Logger;
import ru.ant.rc.serial.exception.CommPortException;
import ru.ant.rc.serial.exception.UnsupportedHardwareException;

import java.io.File;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        File f = new File(".");
        String workingPath = f.getAbsolutePath();
        workingPath = workingPath.substring(0, workingPath.length()-1);

        SerialDriver serialDriver = null;
        try {
            SerialHardwareDetector serialHardwareDetector = new SerialHardwareDetector(new Config(workingPath));
            serialDriver = serialHardwareDetector.getSerialDriver();

            logger.info(serialDriver.sendVectorCommand(0, 0));
        } catch (UnsupportedHardwareException e) {
            logger.error(e.getMessage(), e);
        } catch (CommPortException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (serialDriver != null){
                serialDriver.disconnect();
            }
        }
    }
}
