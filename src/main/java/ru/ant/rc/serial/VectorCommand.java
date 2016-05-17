package ru.ant.rc.serial;

/**Vector style Command extension.
 * Used between SerialDriver and your application.
 * Modify it for your app and put into Queue of SerialService instance.
 * <img alt="SerialDriver functional diagram" src="https://raw.github.com/ant2012/SerialDriver/master/SerialDriverArchitecture.png" />
 * @author Ant
 * @version 1.0
 */
public class VectorCommand extends Command {
    final public int x;
    final public int y;

    /**Parameters x,y - is shift vector of single joystick. To use 2 joysticks see class
     * {@link TractorCommand#TractorCommand TractorCommand}
     * @param timeMillis Timestamp of command.
     *                   Used in SerialService to check obsolete commands
     *                   and to prevent bad order.
     *                   Bad order is possible in case of asynchronous put into Queue.
     *                   For example in case of ajax RC.
     * @param x X joystick shift
     * @param y Y joystick shift
     * @see TractorCommand#TractorCommand TractorCommand
     */
    public VectorCommand(int x, int y, long timeMillis) {
        super(timeMillis);
        this.x = x;
        this.y = y;
    }

    /**Constructs the STOP command instance (x=y=0)
     * @param timeMillis Timestamp of command.
     *                   Used in SerialService to check obsolete commands
     *                   and to prevent bad order.
     *                   Bad order is possible in case of asynchronous put into Queue.
     *                   For example in case of ajax RC.
     */
    public static VectorCommand STOP(long timeMillis) {
        return new VectorCommand(0, 0, timeMillis);
    }

    /**{@inheritDoc}
     *Compares x values & y values
     */
    @Override
    public boolean equals(Object o){
        if (!(o instanceof VectorCommand))return false;
        VectorCommand v = (VectorCommand)o;
        return (v.x == this.x && v.y == this.y);
    }

}
