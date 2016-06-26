package ru.ant.rc.serial;

import static java.lang.Math.round;

/**Power supply descriptor
 * @author Ant
 * @version 1.0
 */
public class Battery {
    private final int MIN_VOLTAGE_VALUE;
    private final int MAX_VOLTAGE_VALUE;
    private final int MIN_VOLTAGE_CALIBRATION;
    private final int MAX_VOLTAGE_CALIBRATION;
    private int rawVoltage = 0;
    private double calibratedVoltage = 0;

    /**Sets measured voltage=0
     */
    public Battery(Config config) {
        this.MIN_VOLTAGE_VALUE = Integer.parseInt(config.getOption(Config.BATTERY_MIN_VOLTAGE));
        this.MAX_VOLTAGE_VALUE = Integer.parseInt(config.getOption(Config.BATTERY_MAX_VOLTAGE));
        this.MIN_VOLTAGE_CALIBRATION = Integer.parseInt(config.getOption(Config.BATTERY_MIN_VOLTAGE_CALIBRATION));
        this.MAX_VOLTAGE_CALIBRATION = Integer.parseInt(config.getOption(Config.BATTERY_MAX_VOLTAGE_CALIBRATION));
    }

    /**Sets measured voltage
     * @param voltageValue The voltage value (milli Volts)
     */
    public void setVoltage(int voltageValue){
        rawVoltage = voltageValue;
        double coef = Double.valueOf(MAX_VOLTAGE_VALUE - MIN_VOLTAGE_VALUE) / (MAX_VOLTAGE_CALIBRATION - MIN_VOLTAGE_CALIBRATION);
        calibratedVoltage = coef * (rawVoltage - MIN_VOLTAGE_CALIBRATION) + MIN_VOLTAGE_VALUE;
    }

    /**Returns voltage level
     * @return Level percentage
     */
    public int getVoltageLevel(){
        return (int) round(100 * (calibratedVoltage - MIN_VOLTAGE_VALUE) / (MAX_VOLTAGE_VALUE - MIN_VOLTAGE_VALUE));
    }

    /**Returns voltage
     * @return Measured voltage (milli Volts)
     */
    public double getVoltage(){
        return calibratedVoltage;
    }

    public int getRawVoltage() {
        return rawVoltage;
    }
}
