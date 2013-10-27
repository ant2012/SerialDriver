package net.ant.rc.serial;

/**Tractor style Command extension.
 * Used between SerialDriver and your application.
 * Modify it for your app and put into Queue of SerialService instance.
 * <img alt="SerialDriver functional diagram" src="https://raw.github.com/ant2012/SerialDriver/master/SerialDriverArchitecture.png" />
 * @author Ant
 * @version 1.0
 */
public class TractorCommand extends Command {
    final public int left;
    final public int right;

    /**
     * @param timeMillis Timestamp of command.
     *                   Used in SerialService to check obsolete commands
     *                   and to prevent bad order.
     *                   Bad order is possible in case of asynchronous put into Queue.
     *                   For example in case of ajax RC.
     * @param left Left joystick speed (Positive values means straight direction. Negatives - forward.)
     * @param right Right joystick speed
     */
    public TractorCommand(int left, int right, long timeMillis) {
        super(timeMillis);
        this.left = left;
        this.right = right;
    }

    /**Constructs the STOP command instance (lef=right=0)
     * @param timeMillis Timestamp of command.
     *                   Used in SerialService to check obsolete commands
     *                   and to prevent bad order.
     *                   Bad order is possible in case of asynchronous put into Queue.
     *                   For example in case of ajax RC.
     */
    public static TractorCommand STOP(long timeMillis) {
        return new TractorCommand(0, 0, timeMillis);
    }

    /**{@inheritDoc}
     *Compares left values & right values
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TractorCommand))return false;
        TractorCommand c = (TractorCommand)o;
        return (c.left == this.left && c.right == this.right);
    }
}
