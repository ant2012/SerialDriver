package ru.ant.rc.serial;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.apache.log4j.Logger;
import ru.ant.rc.serial.exception.CommPortException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.TooManyListenersException;

/**This class is SerialPort listener.
 * @author Ant
 * @version 1.0
 */
public class SerialCommunicator implements SerialPortEventListener{
    private final int NEW_LINE_ASCII = 10;
    private final int LISTENER_TIMEOUT;

    private boolean isReadComplete = false;
    private byte[] receivedData = new byte[200];
    private int receivedCount = 0;
    private final SerialConnection serialConnection;

    private final Logger logger;

    /**Not useful from applications.
     * It initiates by SerialDriver.
     * You don't need to worry about it in application.
     */
    public SerialCommunicator(SerialConnection serialConnection, Config config) {
        this.serialConnection = serialConnection;
        logger = Logger.getLogger(this.getClass());
        LISTENER_TIMEOUT = Integer.parseInt(config.getOption(Config.SERIAL_LISTENER_TIMEOUT));
    }

    /**Runs SerialPort event listener.
     * If event appears, it notifies the {@link #serialEvent(gnu.io.SerialPortEvent) serialEvent} method.
     */
    public void initListener() throws TooManyListenersException {
        SerialPort serialPort = serialConnection.getSerialPort();
        serialPort.removeEventListener();
        serialPort.addEventListener(this);
        serialPort.notifyOnDataAvailable(true);
        try {
            Thread.sleep(LISTENER_TIMEOUT);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void readLineFromInput(){
        InputStream in = serialConnection.getIn();
        if (isReadComplete) return;
        byte byteOfData;
        try {
            while((byteOfData = (byte)in.read()) > -1){
                if (byteOfData == NEW_LINE_ASCII) {
                    isReadComplete = (receivedCount > 0);
                    break;
                } else {
                    receivedData[receivedCount++] = byteOfData;
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Invokes outside the application by SerialPort event.
     * It require the listener is up.
     * @see #initListener()
     */
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            readLineFromInput();
        }else{
            logger.info("Serial event: " + serialPortEvent.getEventType());
        }
    }

    private String checkMessage() {
        String message = null;
        if (isReadComplete) {
            message = new String(receivedData, 0, receivedCount);
            receivedData = new byte[200];
            receivedCount = 0;
            isReadComplete = false;
        }else{
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return message;
    }

    private void sendMessage(String message){
        OutputStream out = serialConnection.getOut();
        try {
            out.write((message + "\n").getBytes());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Sends text through the SerialPort and waits for the answer
     * @param command Text to send
     * @return Firmware answer text
     */
    public String sendCommand(String command) throws CommPortException {
        logger.debug("HW command: "+command);
        this.sendMessage(command);

        String message = null;
        long timestamp = new Date().getTime();
        while (message == null){
            message = this.checkMessage();
            if ((new Date().getTime() - timestamp)> LISTENER_TIMEOUT){
                throw new CommPortException("Answer timeout expired");
            }
        }
        logger.debug("HW answer: "+message);
        return message;
    }
}
