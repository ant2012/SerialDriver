package net.ant.rc.serial;

import gnu.io.*;
import net.ant.rc.serial.arduino2wd.Arduino2WDSerialDriver;
import net.ant.rc.serial.exception.CommPortException;
import net.ant.rc.serial.exception.UnsupportedHardwareException;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 09.02.13
 * Time: 10:14
 * To change this template use File | Settings | File Templates.
 */
public class SerialHardwareDetector {
    public static final int CHASSIS_TYPE_UNDEFINED = 0;
    public static final int CHASSIS_TYPE_ARDUINO_2WD = 1;
    public static final int COMM_OPEN_TIMEOUT = 2000;
    private static final String configFileName = "serial.conf";

    private final String workingPath;
    private final int chassisType;
    private final Logger logger;

    private String portName;

    public SerialPort getSerialPort() {
        return serialPort;
    }

    private SerialPort serialPort;

    public InputStream getIn() {
        return in;
    }

    public OutputStream getOut() {
        return out;
    }

    private InputStream in;
    private OutputStream out;

    public SerialCommunicator getSerialCommunicator() {
        return serialCommunicator;
    }

    private final SerialCommunicator serialCommunicator;

    public SerialDriver getSerialDriver() {
        return serialDriver;
    }

    private SerialDriver serialDriver;

    public SerialHardwareDetector(String workingPath) throws CommPortException, UnsupportedHardwareException {
        this.serialCommunicator = new SerialCommunicator(this);
        logger = Logger.getLogger(this.getClass());
        this.workingPath = workingPath;
        testWorkingPath();

        checkSavedPortName();

        if (serialPort == null) {
            detectCommPort();
        }

        this.chassisType = detectChassisType();

        if (this.chassisType == this.CHASSIS_TYPE_ARDUINO_2WD)
            this.serialDriver = new Arduino2WDSerialDriver(this);
        //Add new hardware here

    }

    private void testWorkingPath() throws CommPortException {
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
            getSavedPortName();
            CommPortIdentifier commPortIdentifier = getPortIdentifier();
            checkPort(commPortIdentifier);
            //Check port by querying Firmware version
            checkFirmwareVersion();
        } catch (NoSuchPortException | CommPortException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void getSavedPortName() throws CommPortException {
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
    }

    private CommPortIdentifier getPortIdentifier() throws CommPortException, NoSuchPortException {
        logger.info("Getting PortID for \"" + portName + "\"..");
        CommPortIdentifier commPortIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        checkPortProperties(commPortIdentifier);
        return commPortIdentifier;
    }

    private void checkPortProperties(CommPortIdentifier commPortIdentifier) throws CommPortException {
        logger.info("Checking port properties..");
        if(CommPortIdentifier.PORT_SERIAL!=commPortIdentifier.getPortType()){
            throw new CommPortException("Wrong port type. Serial port expected.");
        }
        if(commPortIdentifier.isCurrentlyOwned()){
            throw new CommPortException("Port is already in use.");
        }
    }

    private void checkPort(CommPortIdentifier commPortIdentifier) {
        logger.info("Checking port " + portName);
        try {
            openSerialPort(commPortIdentifier);
            logger.info("Port " + portName + " is available. Trying to work with it as Arduino.");
            initStreams();
            this.serialCommunicator.initListener();
        } catch (UnsupportedCommOperationException e) {
            if (serialPort != null)serialPort.close();
            clearPortAttributes();
            logger.error(e.getMessage(), e);
        } catch (PortInUseException e) {
            logger.error(e.getMessage(), e);
        } catch (TooManyListenersException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (CommPortException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void openSerialPort(CommPortIdentifier commPortIdentifier) throws PortInUseException, UnsupportedCommOperationException, CommPortException {
        logger.info("Opening port..");
        CommPort commPort = commPortIdentifier.open(this.getClass().getName(), COMM_OPEN_TIMEOUT);
        logger.info("Checking port properties..");
        if (!(commPort instanceof SerialPort)){
            commPort.close();
            throw new CommPortException("Wrong port type. Serial port expected.");
        }
        serialPort = (SerialPort) commPort;
        logger.info("Setting up the port..");
        serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        serialPort.enableReceiveTimeout(COMM_OPEN_TIMEOUT);
    }

    private void initStreams() throws IOException {
        in = serialPort.getInputStream();
        out = serialPort.getOutputStream();
    }

    private void clearPortAttributes(){
        this.portName = null;
        this.serialPort = null;
        this.in = null;
        this.out = null;
    }

    private void checkFirmwareVersion() throws CommPortException {
        String fwVersion = null;
        try {
            fwVersion = this.serialCommunicator.sendCommand("version");
        } catch (CommPortException e) {
            logger.error(e.getMessage(), e);
        }
        if (fwVersion == null || !fwVersion.startsWith("Arduino")) {
            disconnect();
            String portName = this.portName;
            clearPortAttributes();
            throw new CommPortException("There is no Arduino on " + portName);
        }
        logger.info("Port " + this.portName + " looks like her magesty Arduino!");
        logger.info("Detected: " + fwVersion);
    }

    public void disconnect()
    {
        this.serialPort.removeEventListener();
        this.serialPort.close();
        try {
            this.in.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        try {
            this.out.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String detectCommPort() throws CommPortException {
        logger.info("Trying to detect Arduino on any serial port..");
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
        Vector<CommPortIdentifier> portVector = new Vector<CommPortIdentifier>();
        while (portEnum.hasMoreElements()) {
            portVector.add((CommPortIdentifier) portEnum.nextElement());
        }
        int portCount = portVector.size();
        for(int i=0;i<portCount;i++){
            CommPortIdentifier commPortIdentifier = portVector.get(i);
            try {
                portName = commPortIdentifier.getName();
                logger.info("Checking port:" + portName + "(" + (i+1) + " of " + portCount + ")");
                checkPortProperties(commPortIdentifier);
                checkPort(commPortIdentifier);
                if (serialPort == null) continue;

                //Check port by querying Firmware version
                checkFirmwareVersion();
                saveDetectedPortConfiguration();
                break;
            } catch (CommPortException e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (serialPort == null) throw new CommPortException("Unable to detect Arduino on any COM port");
        return portName;
    }

    private void saveDetectedPortConfiguration() {
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
        String result = serialCommunicator.sendCommand("hardware");
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
