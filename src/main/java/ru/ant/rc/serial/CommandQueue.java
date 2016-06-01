package ru.ant.rc.serial;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by ant on 01.06.2016.
 */
public class CommandQueue extends PriorityBlockingQueue<Command> {
    List<Listener> dataListeners = new ArrayList<>();

    @Override
    public Command poll(long timeout, TimeUnit unit) throws InterruptedException {
        return super.poll(timeout, unit);
    }

    @Override
    public void put(Command command) {
        super.put(command);
        notifyListeners();
    }

    private void notifyListeners() {
        dataListeners.forEach(listener -> listener.queueDataAvailable());
    }

    public void addDataListener(Listener listener){
        dataListeners.add(listener);
    }

//    public boolean removeDataListener(Listener listener){
//        return dataListeners.remove(listener);
//    }

    public void removeAllListeners(){
        dataListeners = new ArrayList<>();
    }
}
