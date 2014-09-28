//Android msg: "010,100,095,120,1,0,0,1,0,0,0,0,+"
//SL1=10,SL2=100,SL3=95,SL4=120,Claw=closed,Symmetry=no,LR=L,Direction=up

//#include <SoftwareSerial.h> 
#include <Servo.h>

/*int bluetoothTx = 3;
 int bluetoothRx = 2;
 SoftwareSerial bluetooth(bluetoothTx, bluetoothRx);
 */

//Variables from android
int Bar1, Bar2, Bar3, Bar4, Close, Symmetry, Arm, Up, Down, Left, Right, Stop; 
//Android input string
String inString ="";
//Complete string received
int readOk=0;
//Servo declarations
Servo servo1R, servo2R, servo3R, servo4R, servo5R, servo1L, servo2L, servo3L, servo4L, servo5L;
//Positions of servos
int pos1L, pos2L, pos3L, pos4L, pos5L, pos1R, pos2R, pos3R, pos4R, pos5R;
//Endstops
//int EndStopL=15, EndStopR=16;
//Motor pins
int dPin_1 = 16;  
int dPin_2 = 12; 
int dPin_3 = 11; 
int dPin_4 = 10; 

/******SETUP FUNCTION*******************************/
void setup(){
  Serial.begin(9600);
  Serial.flush();
  //  pinMode(EndStopR, INPUT);
  //  pinMode(EndStopL, INPUT);
  pinMode(dPin_1, OUTPUT);
  pinMode(dPin_2, OUTPUT);
  pinMode(dPin_3, OUTPUT);
  pinMode(dPin_4, OUTPUT);
  servo1R.attach(2);// Cambiar todas las variables a pines correctos
  servo2R.attach(3);
  servo3R.attach(4);
  servo4R.attach(5);
  servo5R.attach(6);
  servo1L.attach(7);
  servo2L.attach(8);
  servo3L.attach(9);
  servo4L.attach(10);
  servo5L.attach(11);
  //bluetooth.begin(9600);
}

/******BT READ FROM ANDROID*************************/
int readFromAndroid(){
  char toSend =(char) Serial.read();// (char)bluetooth.read();
  inString+=toSend;
  if(toSend=='+'){ 
    //Serial.println(inString);
    return readOk=1;
  }
}

/******PARSE INPUT MESSAGE**************************/
void parseMessage(){
  Bar1=inString.substring(0,3).toInt();
  Bar2=inString.substring(4,7).toInt();
  Bar3=inString.substring(8,11).toInt();
  Bar4=inString.substring(12,15).toInt();
  Close=inString.substring(16,17).toInt();
  Symmetry=inString.substring(18,19).toInt();
  Arm=inString.substring(20,21).toInt();
  Up=inString.substring(22,23).toInt();
  Down=inString.substring(24,25).toInt();
  Left=inString.substring(26,27).toInt();
  Right=inString.substring(28,29).toInt();
  Stop=inString.substring(30,31).toInt();  
}

/******PROCESS DATA*********************************/
void processData(){

  if(Arm==1){  //If right arm is selected, set values to right servos
    pos1R=Bar1;
    pos2R=Bar2;
    pos3R=Bar3;
    pos4R=Bar4; 
  }
  else{        //Else set to left ones
    pos1L=Bar1;
    pos2L=Bar2;
    pos3L=Bar3;
    pos4L=Bar4; 
  }

  if(Symmetry==1){  //If symmetry is checked, set inverted values for each arm's link
    pos1L=Bar1;
    pos1R=-Bar1; 
    pos2L=Bar2;
    pos2R=-Bar2;
    pos3L=Bar3;
    pos3R=-Bar3;
    pos4L=Bar4;
    pos4R=-Bar4;
    pos5L=Close;
    pos5R=Close; 
  }  

}


/******WRITE DATA TO ACTUATORS**********************/
void writeData(){
  /*Serial.print("servo 1L:");
   Serial.println(pos1L);
   Serial.print("servo 1R:");
   Serial.println(pos1R);*/

  servo1R.write(pos1R);
  servo2R.write(pos2R);
  servo3R.write(pos3R);
  servo4R.write(pos4R);
  servo1L.write(pos1L);
  servo2L.write(pos2L);
  servo3L.write(pos3L);
  servo4L.write(pos4L);


  /*if(Close==1 && Arm==1){
   int postmp=0;
   while(1){
   servo5R.write(postmp);
   postmp++;
   if( digitalRead(EndStopR)==HIGH)
   break;
   delay(10);
   } 
   }
   else if(Close==1 && Arm==0){
   int postmp=0;
   while(1){
   servo5L.write(postmp);
   postmp++;
   if( digitalRead(EndStopL)==HIGH)
   break;
   delay(10);
   } 
   }*/

  if(Up==1)
    front();
  if(Down==1)
    back();
  if(Left==1)
    left();
  if(Right==1)
    right();
  if (Stop==1)
    stopAll();

}


/******MAIN EXECUTION LOOP**************************/
void loop(){
  //if(bluetooth.available()){
  if(Serial.available()){
    readFromAndroid();
  }
  if(readOk==1){
    parseMessage();
    processData();
    writeData();  
    inString=""; //Empty message string after using it
    readOk=0;
  }
}


/******BASE MOVEMENT FUNCTIONS**********************/
void  front() {
  digitalWrite(dPin_1, LOW); 
  digitalWrite(dPin_2, HIGH);
  digitalWrite(dPin_3, HIGH); 
  digitalWrite(dPin_4, LOW);
  //Serial.println("front");
}

void back(){
  digitalWrite(dPin_1, HIGH);
  digitalWrite(dPin_2, LOW);
  digitalWrite(dPin_3, LOW);
  digitalWrite(dPin_4, HIGH);
  //Serial.println("back");
}

void  right() {
  digitalWrite(dPin_1, LOW);
  digitalWrite(dPin_2, HIGH);
  digitalWrite(dPin_3, LOW); 
  digitalWrite(dPin_4, HIGH);
  //Serial.println("right");
}

void  left() {
  digitalWrite(dPin_1, HIGH);
  digitalWrite(dPin_2, LOW);
  digitalWrite(dPin_3, HIGH); 
  digitalWrite(dPin_4, LOW);
  //Serial.println("left");
}

void stopAll(){
  digitalWrite(dPin_1, LOW); 
  digitalWrite(dPin_2, LOW);
  digitalWrite(dPin_3, LOW); 
  digitalWrite(dPin_4, LOW); 
  //Serial.println("stop");
}

