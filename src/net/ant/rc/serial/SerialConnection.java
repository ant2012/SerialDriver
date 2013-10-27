package net.ant.rc.serial;

import gnu.io.*;
import net.ant.rc.serial.exception.CommPortException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

/**Holds all connection attributes, manage connects and disconnects.
 * @author Ant
 * @version 1.0
 */
public class SerialConnection {
    private final int COMM_OPEN_TIMEOUT = 2000;

    private String portName;
    private SerialPort serialPort;
    private InputStream in;
    private OutputStream out;
    private final SerialCommunicator serialCommunicator;

    private final Logger logger;

    /**
     * Creates SerialConnection and attached SerialCommunicator
     */
    public SerialConnection() {
        logger = Logger.getLogger(this.getClass());
        this.serialCommunicator = new SerialCommunicator(this);
    }

    /**
     * Opens and tests serial connection to specified commPort
     * @param portName CommPortName to test
     */
    public void init(String portName) throws NoSuchPortException, CommPortException {
        init(getPortIdentifier(portName), portName);
    }

    /**
     * Opens and tests serial connection to specified commPort
     * @param commPortIdentifier CommPort to test
     * @param portName CommPortName to test
     */
    public void init(CommPortIdentifier commPortIdentifier, String portName) throws CommPortException {
        clearPortAttributes();
        this.portName = portName;
        logger.info("Checking port:" + portName);
        checkPortProperties(commPortIdentifier);
        checkPort(commPortIdentifier);
    }

    /**Getter
     * @return Port name
     */
    public String getPortName() {
        return portName;
    }

    /**Getter
     * @return Serial Communicator attached instance
     */
    public SerialCommunicator getSerialCommunicator() {
        return serialCommunicator;
    }

    /**
     * Getter
     * @return SerialPort object
     */
    public SerialPort getSerialPort() {
        return serialPort;
    }

    /**
     * Checks the connection is open (serialPort is not null)
     * @return True\False
     */
    public boolean isConnectionOpened() {
        return this.serialPort != null;
    }

    /**
     * Getter
     * @return SerialPort's Input Stream
     */
    public InputStream getIn() {
        return in;
    }

    /**
     * Getter
     * @return SerialPort's Output Stream
     */
    public OutputStream getOut() {
        return out;
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

    private CommPortIdentifier getPortIdentifier(String portName) throws NoSuchPortException {
        logger.info("Getting PortID for \"" + portName + "\"..");
        return CommPortIdentifier.getPortIdentifier(portName);
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

    /**
     * Closes SerialPort, streams and reset all attributes.
     */
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
        clearPortAttributes();
    }

}
