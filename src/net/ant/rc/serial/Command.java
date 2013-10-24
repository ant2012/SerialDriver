package net.ant.rc.serial;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 17.02.13
 * Time: 5:43
 * To change this template use File | Settings | File Templates.
 */
public abstract class Command implements Comparable {
    final public String commandType;
    final public long timeMillis;

    public Command(String commandType, long timeMillis) {
        this.commandType = commandType;
        this.timeMillis = timeMillis;
    }

    @Override
    public int compareTo(Object o) {
        //this > object => 1
        int result;
        Command obj = (Command)o;
        if (this.timeMillis == obj.timeMillis){
            result = 0;
        } else {
            result = (this.timeMillis > obj.timeMillis)?1:-1;
        }
        return result;
    }

    @Override
    public abstract boolean equals(Object o);
}
