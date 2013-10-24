package net.ant.rc.serial;

/**
 * Created with IntelliJ IDEA.
 * User: Ant
 * Date: 21.10.13
 * Time: 1:24
 */
public class Battery {
    private int MAX_VOLTAGE_VALUE = 11100;//11.1V
    private int MIN_VOLTAGE_VALUE = 9000;//9V
    private int currentVoltage = 0;

    public int checkVoltageRate(int voltageValue){
        setVoltage(voltageValue);
        return checkVoltageRate();
    };

    public int checkVoltageRate(){
        return 100 * (currentVoltage - MIN_VOLTAGE_VALUE) / (MAX_VOLTAGE_VALUE - MIN_VOLTAGE_VALUE);
    };

    public void setVoltage(int voltageValue){
        currentVoltage = voltageValue;
    }

    public int getCurrentVoltage(){
        return currentVoltage;
    }
}
