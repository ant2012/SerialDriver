// Chassis2WD.ino - the arduino-side(Firmware) of 2WD driver
// Моторы подключаются к клеммам M1+,M1-,M2+,M2-
// Motor shield использует четыре контакта 6,5,7,4 для управления моторами 

#define SPEED_RIGHT 6
#define SPEED_LEFT  5 
#define DIR_RIGHT   7
#define DIR_LEFT    4
#define DIRECTION_FORWARD HIGH
#define DIRECTION_BACKWARD LOW
#define SPEED_MAX  255
#define SPEED_MIN  127
#define LEFT  0
#define RIGHT 1
#define ROTATE_CW  0
#define ROTATE_CCW 1
#define STOP_TIMEOUT 3000

const int SPEED_HALF = (SPEED_MAX-SPEED_MIN)/2 + SPEED_MIN;

int leftWheelSpeed;
int rightWheelSpeed;
int leftWheelDirection;
int rightWheelDirection;
int movementDuration = 500;

String inputString = "";
boolean stringComplete = false;

String answer;

unsigned long lastCommandTimestamp;

void clear()
{
  leftWheelSpeed  = 0;
  rightWheelSpeed = 0;
  leftWheelDirection  = LOW;
  rightWheelDirection = LOW;
}

void correctSpeed()
{
  int rightCorrection = 45;
  rightWheelSpeed = rightWheelSpeed + rightCorrection;
  if (rightWheelSpeed > SPEED_MAX)
  {
    leftWheelSpeed = leftWheelSpeed - (rightWheelSpeed - SPEED_MAX);
    rightWheelSpeed = SPEED_MAX;
  }
}

void doMovement()
{
    digitalWrite(DIR_LEFT, leftWheelDirection); 
    digitalWrite(DIR_RIGHT, rightWheelDirection); 
    analogWrite(SPEED_LEFT, leftWheelSpeed);
    analogWrite(SPEED_RIGHT, rightWheelSpeed);
    //delay(movementDuration); 
    //analogWrite(SPEED_LEFT, 0);
    //analogWrite(SPEED_RIGHT, 0);
    //digitalWrite(DIR_LEFT, LOW); 
    //digitalWrite(DIR_RIGHT, LOW);
    //clear();
    lastCommandTimestamp = millis();
}

void digitalMove(int leftSpeed, int leftDirection, int rightSpeed, int rightDirection)
{
  leftWheelSpeed  = leftSpeed;
  rightWheelSpeed = rightSpeed;
  leftWheelDirection  = leftDirection;
  rightWheelDirection = rightDirection;
  //correctSpeed();
  doMovement();
}

void rotate(int direction, int speed)
{
  leftWheelSpeed  = speed;
  rightWheelSpeed = speed;
  if(direction == ROTATE_CW)
  {
    leftWheelDirection  = DIRECTION_FORWARD;
    rightWheelDirection = DIRECTION_BACKWARD;
  } else
  {
    leftWheelDirection  = DIRECTION_BACKWARD;
    rightWheelDirection = DIRECTION_FORWARD;
  }
  //correctSpeed();
  doMovement();
}

void rotateCW(int speed)
{
  rotate(ROTATE_CW, speed);
}

void rotateCCW(int speed)
{
  rotate(ROTATE_CCW, speed);
}

void showVersion()
{
  answer = "Arduino Firmware Version: ANT 0.0.1";
}

void showHardwareType()
{
  answer = "Arduino2WD DFRobot mobile platform";
}

//Copy from https://code.google.com/p/tinkerit/wiki/SecretVoltmeter
long readVcc() {
  long result;
  // Read 1.1V reference against AVcc
  ADMUX = _BV(REFS0) | _BV(MUX3) | _BV(MUX2) | _BV(MUX1);
  delay(2); // Wait for Vref to settle
  ADCSRA |= _BV(ADSC); // Convert
  while (bit_is_set(ADCSRA,ADSC));
  result = ADCL;
  result |= ADCH<<8;
  result = 1126400L / result; // Back-calculate AVcc in mV
  return result;
}

void getVoltage()
{
  answer = String(readVcc(), DEC);
}

//Copy from http://playground.arduino.cc/Main/InternalTemperatureSensor
long GetTemp()
{
  unsigned int wADC;
  long t;
  ADMUX = (_BV(REFS1) | _BV(REFS0) | _BV(MUX3));
  ADCSRA |= _BV(ADEN);
  delay(20);
  ADCSRA |= _BV(ADSC);
  while (bit_is_set(ADCSRA,ADSC));
  wADC = ADCW;
  t = (wADC - 324.31 ) / 1.22 * 1000;
  return (t);
}

void getTemperature() {
  answer = String(GetTemp(), DEC);
}

void setup() 
{
  lastCommandTimestamp = millis();
  Serial.begin(9600);
  inputString.reserve(200);
  pinMode(DIR_RIGHT,   OUTPUT);
  pinMode(SPEED_RIGHT, OUTPUT);
  pinMode(SPEED_LEFT,  OUTPUT);
  pinMode(DIR_LEFT,    OUTPUT);
}

boolean finished = false;
void loop() 
{
  if(millis() - lastCommandTimestamp > STOP_TIMEOUT){
    clear();
    doMovement();
  }
  while (stringComplete)
  {
    stringComplete = false;
    String s = inputString;
    inputString = "";
    finished = false;
    
    answer = "\"" + s + "\" command complete";

    if(s.equalsIgnoreCase("Version"))
    {
        showVersion();
        break;
    }
    if(s.equalsIgnoreCase("Hardware"))
    {
        showHardwareType();
        break;
    }
    if(s.equalsIgnoreCase("Voltage"))
    {
        getVoltage();
        break;
    }
    if(s.equalsIgnoreCase("Temperature"))
    {
        getTemperature();
        break;
    }

    if(s.equalsIgnoreCase("CW"))
    {
        rotateCW(SPEED_HALF);
        break;
    }
    if(s.equalsIgnoreCase("CCW"))
    {
        rotateCCW(SPEED_HALF);
        break;
    }
    if(s.equalsIgnoreCase("RotateCW"))
    {
        rotateCW(SPEED_HALF);
        break;
    }
    if(s.equalsIgnoreCase("RotateCCW"))
    {
        rotateCCW(SPEED_HALF);
        break;
    }
    if(s.indexOf("Digital:")==0)
    {
        int commaPosition = s.indexOf(",");
        int leftDirection, rightDirection, leftSpeed, rightSpeed;
        if (s.charAt(8)=='-'){
            leftDirection = DIRECTION_BACKWARD;
            leftSpeed = (s.substring(9, commaPosition)).toInt();
        }else{
            leftDirection = DIRECTION_FORWARD;
            leftSpeed = (s.substring(8, commaPosition)).toInt();
        }
        if (s.charAt(commaPosition+1)=='-'){
            rightDirection = DIRECTION_BACKWARD;
            rightSpeed = (s.substring(commaPosition+2)).toInt();
        }else{
            rightDirection = DIRECTION_FORWARD;
            rightSpeed = (s.substring(commaPosition+1)).toInt();
        }
        //answer = "leftSpeed=" + leftSpeed + "; rightSpeed=" + rightSpeed;
        //answer = "lD=" + leftDirection + "; rD=" + rightDirection;
        digitalMove(leftSpeed, leftDirection, rightSpeed, rightDirection);
        break;
    }
  }
  
  if (!finished)
  {
    Serial.print(answer + '\n');
    finished = true;
  }
}

void serialEvent() {
  while (Serial.available()) {
    // get the new byte:
    char inChar = (char)Serial.read(); 
    // if the incoming character is a newline, set a flag
    // so the main loop can do something about it:
    if (inChar == '\n') {
      stringComplete = true;
      break;
    } 
    // add it to the inputString:
    inputString += inChar;
  }
}
