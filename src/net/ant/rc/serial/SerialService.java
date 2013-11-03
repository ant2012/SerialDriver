package net.ant.rc.serial;

import net.ant.rc.serial.exception.CommPortException;
import net.ant.rc.serial.exception.UnsupportedHardwareException;
import org.apache.log4j.Logger;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**Class running in separate Thread to listen for commands from application.
 * It uses Queue object to collect commands.
 * <p>
 *   SerialService serialService = new SerialService(serialDriver, commandQueue);<br />
 *   Thread serialServiceThread = new Thread(serialService);<br />
 *   serialServiceThread.start();<br />
 * </p>
 * <img alt="SerialDriver functional diagram" src="https://raw.github.com/ant2012/SerialDriver/master/SerialDriverArchitecture.png" />
 * @author Ant
 * @version 1.0
 */
public class SerialService implements Runnable {

    private final long MAX_QUEUE_SIZE = 20;
    private final long POLL_WAIT_TIMEOUT = 3000;
    private final int RECONNECT_TIMEOUT = 5000;
    private SerialDriver serialDriver;
    private PriorityBlockingQueue<Command> commandQueue;
    private final String workPath;
    private final Logger logger;
    private Command STOP = TractorCommand.STOP(0);
    private Command lastCommand = STOP;
    private boolean serviceStopped = false;
    private boolean errorDetected = false;
    private int queueSize;

    /**
     * Main life-loop of service.
     * It takes your commands from the Queue, checks them and sends to SerialDriver.
     * Not useful from application. It invokes by Thread.start() method.
     */
    @Override
    public void run() {
        logger.info("Starting SerialService..");
        while(!this.serviceStopped){
            while(errorDetected) reConnect(workPath);
            try {
                serialDriver.getChipParameters();
                Command command = this.commandQueue.poll(POLL_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
                //If timeout was expired
                if (command == null) {
                    //if last command was STOP then continue waiting, else go to send STOP
                    if (lastCommand.equals(STOP)) {
                        //logger.info("Lifecycle tick");
                        continue;
                    }
                    command = TractorCommand.STOP(lastCommand.timeMillis);
                }
                queueSize = this.commandQueue.size();

                VectorCommand vectorCommand = null;
                TractorCommand tractorCommand = null;
                String valueForLog = "";
                if (command instanceof VectorCommand) {
                    STOP = VectorCommand.STOP(0);
                    vectorCommand = (VectorCommand) command;
                    valueForLog = vectorCommand.x + "," + vectorCommand.y;
                }
                if (command instanceof TractorCommand) {
                    STOP = TractorCommand.STOP(0);
                    tractorCommand = (TractorCommand) command;
                    valueForLog = tractorCommand.left + "," + tractorCommand.right;
                }

                if (CheckBypass1(command, valueForLog))continue;
                if (CheckBypass2(command, valueForLog))continue;
                if (CheckBypass3(command, valueForLog))continue;

                if (command instanceof VectorCommand) {
                    logger.info(serialDriver.sendVectorCommand(vectorCommand.x, vectorCommand.y));
                }
                if (command instanceof TractorCommand) {
                    logger.info(serialDriver.sendTractorCommand(tractorCommand.left, tractorCommand.right));
                }
                lastCommand = command;
            } catch (CommPortException | InterruptedException e) {
                logger.error(e.getMessage(), e);
                errorDetected = true;
            }
        }
        logger.info("Exit the lifecycle");
   }

    //Bypass the entries older then last sent
    private boolean CheckBypass1(Command command, String valueForLog){
        boolean result = false;
        if (command.timeMillis < lastCommand.timeMillis){
            //logger.info("Bypass1 value of [" + valueForLog + "] for " + command.timeMillis + " < " + lastCommand.timeMillis);
            result = true;
        }
        return result;
    }

    //Bypass the same command
    private boolean CheckBypass2(Command command, String valueForLog){
        boolean result = false;
        if (command.equals(lastCommand)){
            //logger.info("Bypass2 value of [" + valueForLog + "] already sent");
            result = true;
        }
        return result;
    }

    //Bypass entries if queue is too long
    private boolean CheckBypass3(Command command, String valueForLog){
        boolean result = false;
        if (queueSize > MAX_QUEUE_SIZE){
            //logger.info("Bypass3 value of [" + valueForLog + "] for " + queueSize + ">" + MAX_QUEUE_SIZE);
            result = true;
        }
        return result;
    }

    /**
     * @param commandQueue Just create new Queue and then put commands to it
     * @param workPath Path to save detected portName for future fast access
     */
    public SerialService(PriorityBlockingQueue<Command> commandQueue, String workPath) {
        this.commandQueue = commandQueue;
        this.workPath = workPath;
        this.logger = Logger.getLogger(this.getClass());
        reConnect(workPath);
    }

    /**
     * Tries to reconnect robot after lost connection
     * @param workPath Path to save detected portName for future fast access
     */
    public void reConnect(String workPath) {
        try {
            logger.info("SerialService: Trying to reConnect robot..");
            SerialHardwareDetector serialHardwareDetector = new SerialHardwareDetector(workPath);
            this.serialDriver = serialHardwareDetector.getSerialDriver();
            errorDetected = false;
        } catch (CommPortException | UnsupportedHardwareException e) {
            logger.error(e.getMessage());
            errorDetected = true;
            try {
                Thread.sleep(RECONNECT_TIMEOUT);
            } catch (InterruptedException e1) {
                logger.error(e1.getMessage(), e1);
            }
        }
    }

    /**
     * Use it before destroy the Thread
     */
    public void stop(){
        logger.info("Stopping SerialService..");
        this.serviceStopped = true;
        this.serialDriver.disconnect();
    }

    /**
     * Getter
     * @return SerialDriver instance
     */
    public SerialDriver getSerialDriver() {
        return serialDriver;
    }
}
