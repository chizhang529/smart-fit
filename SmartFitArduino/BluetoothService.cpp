/****************************************************************************
 Module
   BluetoothService.c

 Revision
   1.0.1

 Description
   This is bluetooth service under the 
   Gen2 Events and Services Framework.

 Notes


****************************************************************************/

/****************************************************************************
 The Bluetooth related code comes from  bleuart_datamode example code from 
 Adafruit
****************************************************************************/

/*********************************************************************
 This is an example for our nRF51822 based Bluefruit LE modules

 Pick one up today in the adafruit shop!

 Adafruit invests time and resources providing this open source code,
 please support Adafruit and open-source hardware by purchasing
 products from Adafruit!

 MIT license, check LICENSE for more information
 All text above, and the splash screen below must be included in
 any redistribution
*********************************************************************/

/*----------------------------- Include Files -----------------------------*/
/* include header files for the framework and this service
*/
#include "ES_Configure.h"
#include "./Framework/ES_Framework.h"
#include "./Framework/ES_DeferRecall.h"
#include "BluetoothService.h"
#include "LogicService.h"

#include <Arduino.h>
#include <SPI.h>
#if not defined (_VARIANT_ARDUINO_DUE_X_) && not defined (_VARIANT_ARDUINO_ZERO_)
  #include <SoftwareSerial.h>
#endif

#include "Adafruit_BLE.h"
#include "Adafruit_BluefruitLE_SPI.h"
#include "Adafruit_BluefruitLE_UART.h"

#include "BluefruitConfig.h"


/*----------------------------- Module Defines ----------------------------*/
// these times assume a 10.24mS/tick timing
#define ONE_SEC 98
#define HALF_SEC (ONE_SEC/2)
#define QUARTER_SEC (ONE_SEC/4)
#define TEN_MILLI_SEC 1
#define TWO_SEC (ONE_SEC*2)
#define FIVE_SEC (ONE_SEC*5)

#define BLE_DISCONNECT 0
#define BLE_CONNECT 1
#define RX_START 2 //Received from tablet that customer should start scanning tag
#define TX_END 3 //Transmit to tablet that fitting room door is opened and restart app
#define TX_RFID 4 //Transmit to tablet the UID of RFID tag scanned
/*=========================================================================
    APPLICATION SETTINGS

    FACTORYRESET_ENABLE       Perform a factory reset when running this sketch
   
                              Enabling this will put your Bluefruit LE module
                              in a 'known good' state and clear any config
                              data set in previous sketches or projects, so
                              running this at least once is a good idea.
   
                              When deploying your project, however, you will
                              want to disable factory reset by setting this
                              value to 0.  If you are making changes to your
                              Bluefruit LE device via AT commands, and those
                              changes aren't persisting across resets, this
                              is the reason why.  Factory reset will erase
                              the non-volatile memory where config data is
                              stored, setting it back to factory default
                              values.
       
                              Some sketches that require you to bond to a
                              central device (HID mouse, keyboard, etc.)
                              won't work at all with this feature enabled
                              since the factory reset will clear all of the
                              bonding data stored on the chip, meaning the
                              central device won't be able to reconnect.
    MINIMUM_FIRMWARE_VERSION  Minimum firmware version to have some new features
    MODE_LED_BEHAVIOUR        LED activity, valid options are
                              "DISABLE" or "MODE" or "BLEUART" or
                              "HWUART"  or "SPI"  or "MANUAL"
    -----------------------------------------------------------------------*/
    #define FACTORYRESET_ENABLE         1
    #define MINIMUM_FIRMWARE_VERSION    "0.6.6"
    #define MODE_LED_BEHAVIOUR          "MODE"
/*=========================================================================*/



/*---------------------------- Module Functions ---------------------------*/
/* prototypes for private functions for this service.They should be functions
   relevant to the behaviour of this service
*/

static void error(const __FlashStringHelper*err);

/*---------------------------- Module Variables ---------------------------*/
// with the introduction of Gen2, we need a module level Priority variable
static uint8_t MyPriority;
// add a deferral queue for up to 3 pending deferrals +1 to allow for overhead
static ES_Event DeferralQueue[3+1];
// define CurrentBluetoothState
BluetoothState_t CurrentBluetoothState;
// define firstRound as each character consists of two bytes and while ( ble.available() ) will be called twice
static bool firstRound;
// define a state variable indicating whether bluetooth is connected that can be queried by AccelerometerService
static int bleConnection;


// Create the bluefruit object, either software serial...uncomment these lines
SoftwareSerial bluefruitSS = SoftwareSerial(BLUEFRUIT_SWUART_TXD_PIN, BLUEFRUIT_SWUART_RXD_PIN);
Adafruit_BluefruitLE_UART ble(bluefruitSS, BLUEFRUIT_UART_MODE_PIN,
                      BLUEFRUIT_UART_CTS_PIN, BLUEFRUIT_UART_RTS_PIN);


/*------------------------------ Module Code ------------------------------*/
/****************************************************************************
 Function
     InitBluetoothService

 Parameters
     uint8_t : the priorty of this service

 Returns
     bool, false if error in initialization, true otherwise

 Description
     Saves away the priority, and does any 
     other required initialization for this service
 Notes

****************************************************************************/
bool InitBluetoothService ( uint8_t Priority )
{
  ES_Event ThisEvent;
  MyPriority = Priority;
  /********************************************
   in here you write your initialization code
   *******************************************/
  //Serial.println(F("Adafruit Bluefruit Command <-> Data Mode Example"));
  //Serial.println(F("------------------------------------------------"));

  /* Initialise the module */
  Serial.print(F("Initialising the Bluefruit LE module: "));
  if ( !ble.begin(VERBOSE_MODE) )
  {
    error(F("Couldn't find Bluefruit, make sure it's in CoMmanD mode & check wiring?"));
  }
  Serial.println( F("OK!") );

  /*
  if ( FACTORYRESET_ENABLE )
  {
    // Perform a factory reset to make sure everything is in a known state 
    Serial.println(F("Performing a factory reset: "));
    if ( ! ble.factoryReset() ){
      error(F("Couldn't factory reset"));
    }
  }
  */
  
  
  /* Disable command echo from Bluefruit */
  ble.echo(false);

  Serial.println("Requesting Bluefruit info:");
  /* Print Bluefruit information */
  ble.info();

  Serial.println(F("Please use Adafruit Bluefruit LE app to connect in UART mode"));
  Serial.println(F("Then Enter characters to send to Bluefruit"));
  Serial.println();

  ble.verbose(false);  // debug info is a little annoying after this point!

  // set CurrentBluetoothState to connecting
  CurrentBluetoothState = Connecting;

  // set firstRound to true;
  firstRound = true;

  // set bleConnection to ble_disconnect
  bleConnection = BLE_DISCONNECT;
  
  //Serial.println("!oooooooooooooo");
  
  ThisEvent.EventType = ES_INIT;
  if (ES_PostToService( MyPriority, ThisEvent) == true)
  {
      return true;
  }else
  {
      return false;
  }
}

/****************************************************************************
 Function
     PostBluetoothService

 Parameters
     EF_Event ThisEvent ,the event to post to the queue

 Returns
     bool false if the Enqueue operation failed, true otherwise

 Description
     Posts an event to this state machine's queue
 Notes

****************************************************************************/
bool PostBluetoothService( ES_Event ThisEvent )
{
  return ES_PostToService( MyPriority, ThisEvent);
}

/****************************************************************************
 Function
    RunBluetoothService

 Parameters
   ES_Event : the event to process

 Returns
   ES_Event, ES_NO_EVENT if no error ES_ERROR otherwise

 Description
   add your description here
 Notes
   
****************************************************************************/
ES_Event RunBluetoothService( ES_Event ThisEvent )
{
  ES_Event ReturnEvent;
  ReturnEvent.EventType = ES_NO_EVENT; // assume no errors

  switch(CurrentBluetoothState)
  {
    case Connecting:
    if (ThisEvent.EventType == ES_INIT){
      // start a timer to check connection every half second
      ES_Timer_InitTimer(BLE_CHECKING_TIMER, HALF_SEC);
      }
    // now check connection every half second
    if (ThisEvent.EventType == ES_TIMEOUT && ThisEvent.EventParam == BLE_CHECKING_TIMER){
      // if bluetooth is not connected, start connecting timer so as to check connection 0.5 second later
      if (! ble.isConnected()){
        //Serial.println("Check Bluetooth connection again");
        ES_Timer_InitTimer(BLE_CHECKING_TIMER, HALF_SEC);
        }
      // if bluetooth is connected, continue the remaining intialization process and set CurrentBluetoothState to Connected
      else{
        CurrentBluetoothState = Connected;
        bleConnection = BLE_CONNECT;
        Serial.println("State changed: Bluetooth Connected");
        ES_Event Event2Post;
        Event2Post.EventType = ES_BLE_STATE;
        Event2Post.EventParam = BLE_CONNECT;
        PostLogicService(Event2Post);
                  
          // Set module to DATA mode
          //Serial.println( F("Switching to DATA mode!") );
          ble.setMode(BLUEFRUIT_MODE_DATA);
          
          //Serial.println(F("******************************"));
          ES_Timer_InitTimer(BLE_CHECKING_TIMER, QUARTER_SEC);
        }
        
      }
    break;

    case Connected:
    // now check if there is any data sent from Adafruit BLE module to android app
    /*
    if (ThisEvent.EventType == ES_NEW_ACCEL_DATA){
       String string2send = QueryCurrentDataString();
       Serial.println(string2send); 
       //byte byteArray2send[string2send.length()];
       //string2send.getBytes(byteArray2send, string2send.length());
       ble.print(string2send);
    }
    */
     if (ThisEvent.EventType == ES_RFID){
      String string2send;
       switch (ThisEvent.EventParam){
        case 1:
        string2send = "1";//QueryRFIDUID();
        //Serial.println("BLE ready to send 1");
        break;

        case 2:
        string2send = "2";//QueryRFIDUID();
        //Serial.println("BLE ready to send 2");
        break;

        case 3:
        string2send = "3";//QueryRFIDUID();
        //Serial.println("BLE ready to send 3");
        break;
        }
         Serial.print("BLE sending byte: ");
         Serial.println(string2send); 
         ble.print(string2send);
    }
    if (ThisEvent.EventType == ES_BLE_COMMAND && ThisEvent.EventParam == TX_END){
       String string2send = "e";
       Serial.print("BLE sending byte: ");
       Serial.println(string2send); 
       ble.print(string2send);
    }
    // now check if there is any data sent from android app to Adafruit BLE module
    if (ThisEvent.EventType == ES_TIMEOUT && ThisEvent.EventParam == BLE_CHECKING_TIMER){
      // Check for user input
      char n, inputs[BUFSIZE+1];

      /*
      if (Serial.available()){
        n = Serial.readBytes(inputs, BUFSIZE);
        inputs[n] = 0;
        // Send characters to Bluefruit
        Serial.print("Sending: ");
        Serial.println(inputs);
        // Send input data to host via Bluefruit
        ble.print(inputs);
        }
        */
        
        // Echo received data
        while ( ble.available() ){
          int c = ble.read();
          if (/*firstRound == */true){
            Serial.print("Receiving: ");
            Serial.println((char)c);
            // Now do something based on which letter it has received
            
            if ((char)c == 's'){
              //Serial.println("Bluetooth Service: Receiving start command");
              ES_Event Event2Post;
              Event2Post.EventType = ES_BLE_COMMAND;
              Event2Post.EventParam = RX_START;
              PostLogicService(Event2Post);
              }
            }
          firstRound = !firstRound;
          /*
          // Hex output too, helps w/debugging!
          Serial.print(" [0x");
          if (c <= 0xF) Serial.print(F("0"));
          Serial.print(c, HEX);
          Serial.print("] ");
          */
          }
      // Check Bluetooth Connection again
      // Once connection is lost, go back to Connecting state
      if (! ble.isConnected()){
        CurrentBluetoothState = Connecting;
        bleConnection = BLE_DISCONNECT;
        Serial.println("State changed: Bluetooth Disconnected");
        ES_Event Event2Post;
        Event2Post.EventType = ES_BLE_STATE;
        Event2Post.EventParam = BLE_DISCONNECT;
        PostLogicService(Event2Post);
        // start a timer to check connection every half second
        ES_Timer_InitTimer(BLE_CHECKING_TIMER, HALF_SEC);
        }
      ES_Timer_InitTimer(BLE_CHECKING_TIMER, QUARTER_SEC);
    }
    break;

    default:
    break;
  }
  return ReturnEvent;
}

int QueryBleConnection(void){
  return bleConnection;}
/***************************************************************************
 private functions
 ***************************************************************************/
static void error(const __FlashStringHelper*err) {
  Serial.println(err);
  while (1);
}

/*------------------------------- Footnotes -------------------------------*/
/*------------------------------ End of file ------------------------------*/

