package net.ant.rc.serial;

/**Power supply descriptor
 * @author Ant
 * @version 1.0
 */
public class Battery {
    private final int MIN_VOLTAGE_VALUE = 9000;//9V
    private final int MAX_VOLTAGE_VALUE = 11100;//11.1V
    private int currentVoltage;

    /**Sets measured voltage=0
     */
    public Battery() {
        this.currentVoltage = 0;
    }

    /**Sets measured voltage
     * @param voltageValue The voltage value (milli Volts)
     */
    public void setVoltage(int voltageValue){
        currentVoltage = voltageValue;
    }

    /**Returns voltage level
     * @return Level percentage
     */
    public int checkVoltageLevel(){
        return 100 * (currentVoltage - MIN_VOLTAGE_VALUE) / (MAX_VOLTAGE_VALUE - MIN_VOLTAGE_VALUE);
    }

    /**Sets measured voltage and Returns voltage level
     * @param voltageValue The voltage value
     * @return Level percentage
     */
    public int checkVoltageLevel(int voltageValue){
        setVoltage(voltageValue);
        return checkVoltageLevel();
    }

    /**Returns voltage
     * @return Measured voltage (milli Volts)
     */
    public int getCurrentVoltage(){
        return currentVoltage;
    }
}
