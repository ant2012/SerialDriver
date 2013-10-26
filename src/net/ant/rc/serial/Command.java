package net.ant.rc.serial;

/**Command base class.
 * Used between SerialDriver and your application.
 * Extend it for your app and put into Queue of SerialService instance.
 * <img src="https://raw.github.com/ant2012/SerialDriver/master/SerialDriverArchitecture.png" />
 * @author Ant
 * @version 1.0
 */
public abstract class Command implements Comparable {
    final public long timeMillis;

    /**
     * @param timeMillis Timestamp of command.
     *                   Used in SerialService to check obsolete commands
     *                   and to prevent bad order.
     *                   Bad order is possible in case of asynchronous put into Queue.
     *                   For example in case of ajax RC.
     */
    public Command(long timeMillis) {
        this.timeMillis = timeMillis;
    }

    /**{@inheritDoc}*/
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

    /**{@inheritDoc}*/
    @Override
    public abstract boolean equals(Object o);
}
