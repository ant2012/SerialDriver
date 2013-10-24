package net.ant.rc.serial;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import net.ant.rc.serial.exception.CommPortException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.TooManyListenersException;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 29.01.13
 * Time: 2:21
 * To change this template use File | Settings | File Templates.
 */
public class SerialCommunicator implements SerialPortEventListener{
    private static final int NEW_LINE_ASCII = 10;
    private static final int LISTENER_INIT_TIMEOUT = 5000;

    private boolean isReadComplete = false;
    private byte[] receivedData = new byte[200];
    private int receivedCount = 0;
    private final SerialHardwareDetector serialHardwareDetector;

    private final Logger logger;

    public SerialCommunicator(SerialHardwareDetector serialHardwareDetector) {
        this.serialHardwareDetector = serialHardwareDetector;
        logger = Logger.getLogger(this.getClass());
    }

    void initListener() throws TooManyListenersException {
        SerialPort serialPort = serialHardwareDetector.getSerialPort();
        serialPort.removeEventListener();
        serialPort.addEventListener(this);
        serialPort.notifyOnDataAvailable(true);
        try {
            Thread.sleep(LISTENER_INIT_TIMEOUT);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void readLineFromInput(){
        InputStream in = serialHardwareDetector.getIn();
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
        OutputStream out = serialHardwareDetector.getOut();
        try {
            out.write((message + "\n").getBytes());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String sendCommand(String command) throws CommPortException {
        this.sendMessage(command);

        String message = null;
        long timestamp = new Date().getTime();
        while (message == null){
            message = this.checkMessage();
            if ((new Date().getTime() - timestamp)> LISTENER_INIT_TIMEOUT){
                throw new CommPortException("Answer timeout expired");
            }
        }
        return message;
    }
}
