package ru.ant.rc.serial;

import org.apache.log4j.Logger;
import ru.ant.rc.serial.exception.CommPortException;
import ru.ant.rc.serial.exception.UnsupportedHardwareException;

import java.util.Date;
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
public class SerialService implements Listener {
    private Logger log = Logger.getLogger(getClass());
    private static SerialService ourInstance = new SerialService();
    public static SerialService getInstance() {
        return ourInstance;
    }

    private SerialService() {
        commandQueue = new CommandQueue();
        commandQueue.addDataListener(this);

        config = new Config();
        MAX_QUEUE_SIZE = Integer.parseInt(config.getOption(Config.SERVICE_MAX_QUEUE_SIZE));
        POLL_WAIT_TIMEOUT = Integer.parseInt(config.getOption(Config.SERVICE_POLL_WAIT_TIMEOUT));
        RECONNECT_TIMEOUT = Integer.parseInt(config.getOption(Config.SERVICE_RECONNECT_TIMEOUT));
        SLEEP_TIMEOUT = Integer.parseInt(config.getOption(Config.SLEEP_TIMEOUT));
    }

    private final int MAX_QUEUE_SIZE;
    private final int POLL_WAIT_TIMEOUT;
    private final int RECONNECT_TIMEOUT;
    private final int SLEEP_TIMEOUT;
    private SerialDriver serialDriver;
    private final CommandQueue commandQueue;
    private Listener queueListener;
    private Command STOP = TractorCommand.STOP(0);
    private Command lastCommand = STOP;
    private boolean serviceStopped = true;
    private boolean serviceStopping = false;
    private boolean disconnected = true;
    private boolean serialListenerPaused = false;
    private int queueSize;
    private final Config config;

    /**
     * Main life-loop of service.
     * It takes your commands from the Queue, checks them and sends to SerialDriver.
     * Not useful from application. It invokes by Thread.start() method.
     */
    public void run() {
        log.info("Starting SerialService..");
//        reConnect();
        long lastCommandTime = new Date().getTime();
        while(!this.serviceStopping){
            while(disconnected && !this.serviceStopping) reConnect();
            if(this.serviceStopping) break;
            try {
                long now = new Date().getTime();
                serialDriver.getArduinoState().refresh();
                Command command = this.commandQueue.poll(POLL_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
                //If timeout was expired
                if (command == null) {
                    //if last command was STOP then continue waiting, else go to send STOP
                    if (lastCommand.equals(STOP)) {
                        log.debug("Lifecycle tick");
                        if( now - lastCommandTime > SLEEP_TIMEOUT){
                            log.info("Pause serial listener");
                            pauseSerialListener();
                            break;
                        }
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

                if (CheckBypass1(command, valueForLog) || CheckBypass2(command, valueForLog) || CheckBypass3(command, valueForLog)){
                    lastCommand = command;
                    lastCommandTime = now;
                    continue;
                }

                if (command instanceof VectorCommand) {
                    log.info(serialDriver.sendVectorCommand(vectorCommand.x, vectorCommand.y));
                }
                if (command instanceof TractorCommand) {
                    log.info(serialDriver.sendTractorCommand(tractorCommand.left, tractorCommand.right));
                }

                lastCommand = command;
                lastCommandTime = now;

            } catch (CommPortException | InterruptedException e) {
                log.error(e.getMessage(), e);
                disconnected = true;
            } catch (Exception e){
                setStopped();
                log.info("Exit the lifecycle");
                throw e;
            }
        }
        if(serviceStopping) System.out.println();
        setStopped();
        log.info("Exit the lifecycle");
   }

    private void setStopped() {
        serviceStopped = true;
        serviceStopping = false;
    }

    private void setRunning() {
        serviceStopped = false;
        serviceStopping = false;
    }

    private void setStopping() {
        serviceStopping = true;
    }

    public void start(){
        if(!serviceStopped || serviceStopping) return;
        setRunning();
        resumeSerialListener();
        run();
//        new Thread(this).start();
    }

    private void pauseSerialListener() {
        serialDriver.pause();
        serialListenerPaused = true;
    }

    private void resumeSerialListener() {
        if(!serialListenerPaused) return;
        serialListenerPaused = false;
        serialDriver.resume();
    }


    //Bypass the entries older then last sent
    private boolean CheckBypass1(Command command, String valueForLog){
        boolean result = false;
        if (command.timeMillis < lastCommand.timeMillis){
            //log.info("Bypass1 value of [" + valueForLog + "] for " + command.timeMillis + " < " + lastCommand.timeMillis);
            result = true;
        }
        return result;
    }

    //Bypass the same command
    private boolean CheckBypass2(Command command, String valueForLog){
        boolean result = false;
        if (command.equals(lastCommand)){
            //log.info("Bypass2 value of [" + valueForLog + "] already sent");
            result = true;
        }
        return result;
    }

    //Bypass entries if queue is too long
    private boolean CheckBypass3(Command command, String valueForLog){
        boolean result = false;
        if (queueSize > MAX_QUEUE_SIZE){
            //log.info("Bypass3 value of [" + valueForLog + "] for " + queueSize + ">" + MAX_QUEUE_SIZE);
            result = true;
        }
        return result;
    }

    /**
     * Tries to reconnect robot after lost connection
     */
    private void reConnect() {
        try {
            log.info("SerialService: Trying to reConnect robot..");
            SerialHardwareDetector serialHardwareDetector = new SerialHardwareDetector(config);
            this.serialDriver = serialHardwareDetector.getSerialDriver();
            disconnected = false;
        } catch (CommPortException | UnsupportedHardwareException e) {
            log.error(e.getMessage());
            disconnected = true;
            try {
                Thread.sleep(RECONNECT_TIMEOUT);
            } catch (InterruptedException e1) {
                log.error(e1.getMessage(), e1);
            }
        }
    }

    /**
     * Use it before destroy the Thread
     */
    public void stop(){
        if(serviceStopping || serviceStopped) return;
        setStopping();
        log.info("Stopping SerialService..");

        while(!serviceStopped) {
            try {
                System.out.print(".");
                Thread.sleep(POLL_WAIT_TIMEOUT);
            } catch (InterruptedException e) {
                log.error("Sleep error", e);
            }
        }

        serialDriver.pause();
    }

//    public void stopNowait(){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                SerialService.this.stop();
//            }
//        }).start();
//    }

    /**
     * Getter
     * @return SerialDriver instance
     */
    public SerialDriver getSerialDriver() {
        return serialDriver;
    }

    public CommandQueue getCommandQueue() {
        return commandQueue;
    }

//    public boolean isStopped() {
//        return serviceStopped;
//    }

    public String getStatus() {
        if(serviceStopped) return "stopped";
        if(serviceStopping) return "stopping..";
        return "running";
    }

    public void destroy(){
        stop();

        disconnect();

        commandQueue.removeAllListeners();
    }

    private void disconnect(){
        if(serialDriver !=null)
            serialDriver.disconnect();
        disconnected = true;
    }

    @Override
    public void queueDataAvailable() {
//        start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                SerialService.this.start();
            }
        }).start();

    }

}
