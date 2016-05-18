package ru.ant.rc.serial;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import org.apache.log4j.Logger;
import ru.ant.rc.serial.arduino2wd.Arduino2WDSerialDriver;
import ru.ant.rc.serial.exception.CommPortException;
import ru.ant.rc.serial.exception.UnsupportedHardwareException;

import java.util.Enumeration;
import java.util.Vector;

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
    private final int chassisType;
    private final Logger logger;
    private SerialDriver serialDriver;
    private Config config;

    /**
     * @return SerialDriver instance
     */
    public SerialDriver getSerialDriver() {
        return serialDriver;
    }

    /**
     * @param config Just pass new Config()
     */
    public SerialHardwareDetector(Config config) throws CommPortException, UnsupportedHardwareException {
        this.config = config;
        this.serialConnection = new SerialConnection(config);
        logger = Logger.getLogger(this.getClass());

        checkSavedPortName();

        if (!serialConnection.isConnectionOpened()) {
            detectCommPort();
        }

        chassisType = detectChassisType();

        //Add new hardware here
        if (chassisType == CHASSIS_TYPE_ARDUINO_2WD)
            serialDriver = new Arduino2WDSerialDriver(serialConnection, config);

    }

    private void checkSavedPortName(){
        try {
            String portName = config.getOption(Config.COMM_PORT_NAME);
            if (portName==null){
                logger.warn("CommPortName not found in configuration file. Nothing to check.");
                return;
            }
            serialConnection.init(portName);
            //Check port by querying Firmware version
            checkFirmwareVersion();
        } catch (NoSuchPortException e) {
            logger.error(e.toString());
        } catch (CommPortException e) {
            logger.error(e.getMessage());
        }
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
        config.setOption(Config.COMM_PORT_NAME, portName, "Automatically detected port configuration");
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
