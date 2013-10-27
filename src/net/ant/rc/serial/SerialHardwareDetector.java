package net.ant.rc.serial;

import gnu.io.*;
import net.ant.rc.serial.arduino2wd.Arduino2WDSerialDriver;
import net.ant.rc.serial.exception.CommPortException;
import net.ant.rc.serial.exception.UnsupportedHardwareException;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**Detects robot on any comm port.
 * Check it's type and init appropriate SerialDriver.
 * Class holds all connection attributes, manage connects and disconnects.
 * <img alt="SerialDriver functional diagram" src="https://raw.github.com/ant2012/SerialDriver/master/SerialDriverArchitecture.png" />
 * @author Ant
 * @version 1.0
 */
public class SerialHardwareDetector {
    private final int CHASSIS_TYPE_UNDEFINED = 0;
    private final int CHASSIS_TYPE_ARDUINO_2WD = 1;
    private final String configFileName = "serial.conf";

    private final SerialConnection serialConnection;
    private final String workingPath;
    private final int chassisType;
    private final Logger logger;
    private SerialDriver serialDriver;

    /**
     * @return SerialDriver instance
     */
    public SerialDriver getSerialDriver() {
        return serialDriver;
    }

    /**
     * @param workingPath Path to save detected portName for future fast access
     */
    public SerialHardwareDetector(String workingPath) throws CommPortException, UnsupportedHardwareException {
        this.serialConnection = new SerialConnection();
        logger = Logger.getLogger(this.getClass());
        this.workingPath = workingPath;
        testWorkingPath();

        checkSavedPortName();

        if (!serialConnection.isConnectionOpened()) {
            detectCommPort();
        }

        this.chassisType = detectChassisType();

        if (this.chassisType == this.CHASSIS_TYPE_ARDUINO_2WD)
            this.serialDriver = new Arduino2WDSerialDriver(this.serialConnection);
        //Add new hardware here

    }

    private void testWorkingPath() {
        try {
            String fileName = this.workingPath + "test.conf";
            FileOutputStream o = new FileOutputStream(fileName);
            Properties p = new Properties();
            p.setProperty("test", "Test");
            p.store(o, "Test workingPath");
            o.close();

            //Cleanup
            File f = new File(fileName);
            f.delete();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void checkSavedPortName(){
        try {
            String portName = getSavedPortName();
            serialConnection.init(portName);
            //Check port by querying Firmware version
            checkFirmwareVersion();
        } catch (NoSuchPortException | CommPortException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String getSavedPortName() throws CommPortException {
        String portName = null;
        logger.info("Searching for port name configuration..");
        //logger.info("Load properties: " + this.workingPath + "/" + this.configFileName);
        Properties config = new Properties();
        //logger.info("First try loading from the current directory");
        try {
            InputStream in = new FileInputStream(this.workingPath + this.configFileName);
            config.load(in);
            in.close();
            portName = config.getProperty("CommPortName");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        if (portName==null){
            throw new CommPortException("CommPortName is not found in " + this.workingPath + this.configFileName);
        }
        return portName;
    }

    private void checkFirmwareVersion() throws CommPortException {
        String fwVersion = null;
        try {
            fwVersion = serialConnection.getSerialCommunicator().sendCommand("version");
        } catch (CommPortException e) {
            logger.error(e.getMessage(), e);
        }
        if (fwVersion == null || !fwVersion.startsWith("Arduino")) {
            String portName = serialConnection.getPortName();
            serialConnection.disconnect();
            throw new CommPortException("There is no Arduino on " + portName);
        }
        logger.info("Port " + serialConnection.getPortName() + " looks like her majesty Arduino!");
        logger.info("Detected: " + fwVersion);
    }

    private void detectCommPort() throws CommPortException {
        logger.info("Trying to detect Arduino on any serial port..");
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
        Vector<CommPortIdentifier> portVector = new Vector<>();
        while (portEnum.hasMoreElements()) {
            portVector.add((CommPortIdentifier) portEnum.nextElement());
        }
        int portCount = portVector.size();
        for(int i=0;i<portCount;i++){
            CommPortIdentifier commPortIdentifier = portVector.get(i);
            try {
                String portName = commPortIdentifier.getName();
                logger.info("Checking port:" + portName + "(" + (i + 1) + " of " + portCount + ")");
                serialConnection.init(commPortIdentifier, portName);
                if (!serialConnection.isConnectionOpened()) continue;

                //Check port by querying Firmware version
                checkFirmwareVersion();
                saveDetectedPortConfiguration(portName);
                break;
            } catch (CommPortException e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (!serialConnection.isConnectionOpened()) throw new CommPortException("Unable to detect Arduino on any COM port");
    }

    private void saveDetectedPortConfiguration(String portName) {
        logger.info("Saving " + portName + " to configuration file for future runs");
        Properties config = new Properties();
        config.setProperty("CommPortName", portName);
        FileOutputStream out;
        try {
            String fileName = workingPath + configFileName;
            out = new FileOutputStream(fileName);
            config.store(out, "Automatically detected port configuration");
            out.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private int detectChassisType() throws CommPortException, UnsupportedHardwareException {
        String result = serialConnection.getSerialCommunicator().sendCommand("hardware");
        int chassisType = CHASSIS_TYPE_UNDEFINED;
        if (result.startsWith("Arduino2WD"))
            chassisType = CHASSIS_TYPE_ARDUINO_2WD;
        //Add new platform here

        if (chassisType == CHASSIS_TYPE_UNDEFINED) {
            if (result.startsWith("Arduino"))
                throw new UnsupportedHardwareException("Chassis of type " + result + " is not supported by this driver version");
            else
                throw new UnsupportedHardwareException("\"Hardware\" command is not supported by Firmware");
        }
        logger.info("Hardware detected: " + result);
        return chassisType;
    }

}
