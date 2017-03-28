#include <SPI.h>
#include <Ethernet.h>
#include <EthernetUdp.h>  // UDP library from: bjoern@cs.stanford.edu 12/30/2008

char buf[8];
float lastTime, currentTime, difference;
const int pinInt0 = 2;
int cont=0;
int RPMCounter=0;
int incomingByte = 0;
int velocity = 20000;
int x = 0;

// ETHERNET MANAGING
byte mac[] = {
  0x98, 0x4F, 0xEE, 0x00, 0x78, 0x1D
};
IPAddress ip(192, 168, 1, 177);
IPAddress ip2(192,168,1,20); 
unsigned int localPort = 8888; 

// buffers for receiving and sending data
char packetBuffer[8]; 

// An EthernetUDP instance to let us send and receive packets over UDP
EthernetUDP Udp;

void setup() 
{
  //Pines Driver
  pinMode(3, OUTPUT_FAST);  // CLK - OUTPUT_FAST only available on pins 2 and 3. 477Khz. Other way it will be 200hz
  pinMode(4, OUTPUT); // ENAB
  pinMode(5, OUTPUT); // DIR

  //Interrupciones
  pinMode(pinInt0, INPUT_PULLUP); 
  attachInterrupt(pinInt0, InterruptISR, RISING);  
  
  digitalWrite(4, LOW);   
  digitalWrite(5, HIGH);  // DIR == 1 (clockwise rotation)
  
  currentTime = millis();
  
  // ETHERNET MANAGING
  Ethernet.begin(mac, ip);
  Udp.begin(localPort);
  
  Serial.begin(9600);
  Serial.println("--- Program start ---");
  Serial.println();
}
 
void loop()
{
  // If there's data available, read a packet
  int packetSize = Udp.parsePacket();
  if (packetSize) {
    
    Udp.read(packetBuffer, 8);
    
    String packet(packetBuffer);

    if (packet.equals("+")) velocity-=100; // "+" character
    else if (packet.equals("-")) velocity+=100; // "-" character
    else if (packet.equals("0")) slowStop();
    else if (packet.equals("1")) slowStart();
    else if (packet.startsWith("v")) velocity = setVelocity(packet.substring(1));
    else if (packet.startsWith("cd")) changeDirection(packet.substring(2));
    
    memset(packetBuffer, 0, sizeof(packetBuffer));
  }
  
  cont=0;
  while(cont<(velocity)) // "Wait loop" to reduce frecency
  {
    cont++;
  }
  x=!x;
  digitalWrite( 3, x);
}


int setVelocity(String velocityString){
  int velocity = velocityString.toInt();

  return 2900000/velocity;
}

void slowStart(){
  digitalWrite(4, HIGH);
  int speedFactor = 12000000/velocity;
  for(int i = 0; i<speedFactor; i++){ //4 vueltas
    cont=0;
    while(cont<velocity*2-((velocity/speedFactor)*i)) cont++;
    
    x=!x;
    digitalWrite(3, x);
  }
}

void slowStop(){
  int speedFactor = 12000000/velocity;
  for(int i = 0; i<speedFactor; i++){ //4 vueltas
    cont=0;
    while(cont<velocity+((velocity/speedFactor)*i)) cont++;
    
    x=!x;
    digitalWrite(3, x);
  }
  digitalWrite(4, LOW);
}

void changeDirection(String directionString){
  int dir = directionString.toInt();
  slowStop();
  delay(500);
  digitalWrite(5, dir);
  slowStart();
}

/// --------------------------
/// Custom PIN ISR Routine
/// --------------------------
void InterruptISR()
{
  //Se entra en la rutina cada media vuelta, luego 20/2 = 10 vueltas
  if(RPMCounter>=20){
      currentTime = millis();
      difference = (currentTime-lastTime)/1000; //Tiempo en segundos
      
      snprintf(buf, 8, "%f", 600/difference);

      Udp.beginPacket(ip2, 6000);
      Udp.write(buf);
      Udp.endPacket();
      
      lastTime = currentTime;
      RPMCounter=0;
  }
  RPMCounter++;
}



