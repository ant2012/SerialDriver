// Chassis2WD.ino - the arduino-side(Firmware) of 2WD driver
// Моторы подключаются к клеммам M1+,M1-,M2+,M2-
// Motor shield использует четыре контакта 6,5,7,4 для управления моторами 

#define SPEED_RIGHT 6
#define SPEED_LEFT  5 
#define DIR_RIGHT   7
#define DIR_LEFT    4
#define DIRECTION_FORWARD HIGH
#define DIRECTION_BACKWARD LOW
#define SPEED_MAX  150
#define SPEED_MIN  127
#define LEFT  0
#define RIGHT 1
#define ROTATE_CW  0
#define ROTATE_CCW 1
#define STOP_TIMEOUT 3000
#define VERSION "0.0.2"

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
  answer = VERSION;
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

// Helper function for free ram.
//   With use of http://playground.arduino.cc/Code/AvailableMemory
//
int freeRam(void)
{
  extern unsigned int __heap_start;
  extern void *__brkval;

  int free_memory;
  int stack_here;

  if (__brkval == 0)
    free_memory = (int) &stack_here - (int) &__heap_start;
  else
    free_memory = (int) &stack_here - (int) __brkval; 

  return (free_memory);
}

void getFreeRAM() {
  answer = String(freeRam(), DEC);
}

void getTotalRAM() {
  answer = String(RAMEND+1, DEC);
}

// Helper function for sketch size.
// The sketch size is runtime calculated.
// From user "Coding Badly" in his post:
//   http://arduino.cc/forum/index.php/topic,115870.msg872309.html#msg872309
// Changed into unsigned long for code size larger than 64kB.
//
// This function returns the sketch size 
// for a size between 0 and 32k. If the code
// size is larger (for example with an Arduino Mega),
// the return value is not valid.
//
unsigned long sketchSize(void)
{
  extern int _etext;
  extern int _edata;

  return ((unsigned long)(&_etext) + ((unsigned long)(&_edata) - 256L));
}

void getSketchSize() {
  answer = String(sketchSize(), DEC);
}

void getTotalFlash() {
  answer = String(((long)FLASHEND)+1, DEC);
}

void getGccVersion() {
  answer = __VERSION__;
}

void getLibcVersion() {
  answer = __AVR_LIBC_VERSION_STRING__;
}

void getSketchSourceName() {
  answer = __FILE__;
}

void getCompileDate() {
  answer = String(__DATE__) + " " + String(__TIME__);
}

void getUpTime() {
  answer = String(millis(), DEC);
}

void getGccCpuTarget() {
#if defined (__AVR_ATtiny45__)
  answer = "AVR_ATtiny45";
#elif defined (__AVR_ATtiny85__)
  answer = "AVR_ATtiny85";
#elif defined (__AVR_ATtiny2313__)
  answer = "AVR_ATtiny2313";
#elif defined (__AVR_ATtiny2313A__)
  answer = "AVR_ATtiny2313A";
#elif defined (__AVR_ATmega48__)
  answer = "AVR_ATmega48";
#elif defined (__AVR_ATmega48A__)
  answer = "AVR_ATmega48A";
#elif defined (__AVR_ATmega48P__)
  answer = "AVR_ATmega48P";
#elif defined (__AVR_ATmega8__)
  answer = "AVR_ATmega8";
#elif defined (__AVR_ATmega8U2__)
  answer = "AVR_ATmega8U2";
#elif defined (__AVR_ATmega88__)
  answer = "AVR_ATmega88";
#elif defined (__AVR_ATmega88A__)
  answer = "AVR_ATmega88A";
#elif defined (__AVR_ATmega88P__)
  answer = "AVR_ATmega88P";
#elif defined (__AVR_ATmega88PA__)
  answer = "AVR_ATmega88PA";
#elif defined (__AVR_ATmega16__)
  answer = "AVR_ATmega16";
#elif defined (__AVR_ATmega168__)
  answer = "AVR_ATmega168";
#elif defined (__AVR_ATmega168A__)
  answer = "AVR_ATmega168A";
#elif defined (__AVR_ATmega168P__)
  answer = "AVR_ATmega168P";
#elif defined (__AVR_ATmega32__)
  answer = "AVR_ATmega32";
#elif defined (__AVR_ATmega328__)
  answer = "AVR_ATmega328";
#elif defined (__AVR_ATmega328P__) 
  answer = "AVR_ATmega328P";
#elif defined (__AVR_ATmega32U2__)
  answer = "AVR_ATmega32U2";
#elif defined (__AVR_ATmega32U4__)
  answer = "AVR_ATmega32U4";
#elif defined (__AVR_ATmega32U6__)
  answer = "AVR_ATmega32U6";
#elif defined (__AVR_ATmega128__)
  answer = "AVR_ATmega128";
#elif defined (__AVR_ATmega1280__)
  answer = "AVR_ATmega1280";
#elif defined (__AVR_ATmega2560__)
  answer = "AVR_ATmega2560";
#endif
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
    if(s.equalsIgnoreCase("FreeRAM"))
    {
        getFreeRAM();
        break;
    }
    if(s.equalsIgnoreCase("TotalRAM"))
    {
        getTotalRAM();
        break;
    }
    if(s.equalsIgnoreCase("SketchSize"))
    {
        getSketchSize();
        break;
    }
    if(s.equalsIgnoreCase("TotalFlash"))
    {
        getTotalFlash();
        break;
    }
    if(s.equalsIgnoreCase("GccVersion"))
    {
        getGccVersion();
        break;
    }
    if(s.equalsIgnoreCase("LibcVersion"))
    {
        getLibcVersion();
        break;
    }
    if(s.equalsIgnoreCase("SketchSourceName"))
    {
        getSketchSourceName();
        break;
    }
    if(s.equalsIgnoreCase("CompileDate"))
    {
        getCompileDate();
        break;
    }
    if(s.equalsIgnoreCase("UpTime"))
    {
        getUpTime();
        break;
    }
    if(s.equalsIgnoreCase("GccCpuTarget"))
    {
        getGccCpuTarget();
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
