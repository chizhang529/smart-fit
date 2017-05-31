/****************************************************************************
 Module
   RFIDService.c

 Revision
   1.0.1

 Description
   This is LED Service under the 
   Gen2 Events and Services Framework.

 Notes

 History

****************************************************************************/
/*----------------------------- Include Files -----------------------------*/
/* include header files for the framework and this service
*/
#include "ES_Configure.h"
#include "./Framework/ES_Framework.h"
#include "./Framework/ES_DeferRecall.h"
#include "RFIDService.h"
#include "LogicService.h"

/*----------------------------- Module Defines ----------------------------*/
// these times assume a 10.24mS/tick timing
#define FIVE_SEC ONE_SEC*5
#define ONE_SEC 98
#define HALF_SEC (ONE_SEC/2)
#define QUARTER_SEC (ONE_SEC/4)
#define ONE_EIGHTH_SEC (ONE_SEC/8)
#define TWO_SEC (ONE_SEC*2)
#define FIVE_SEC (ONE_SEC*5)

/**************************************************************************/
/*! 
    @file     readMifare.pde
    @author   Adafruit Industries
  @license  BSD (see license.txt)

    This example will wait for any ISO14443A card or tag, and
    depending on the size of the UID will attempt to read from it.
   
    If the card has a 4-byte UID it is probably a Mifare
    Classic card, and the following steps are taken:
   
    - Authenticate block 4 (the first block of Sector 1) using
      the default KEYA of 0XFF 0XFF 0XFF 0XFF 0XFF 0XFF
    - If authentication succeeds, we can then read any of the
      4 blocks in that sector (though only block 4 is read here)
   
    If the card has a 7-byte UID it is probably a Mifare
    Ultralight card, and the 4 byte pages can be read directly.
    Page 4 is read by default since this is the first 'general-
    purpose' page on the tags.


This is an example sketch for the Adafruit PN532 NFC/RFID breakout boards
This library works with the Adafruit NFC breakout 
  ----> https://www.adafruit.com/products/364
 
Check out the links above for our tutorials and wiring diagrams 
These chips use SPI or I2C to communicate.

Adafruit invests time and resources providing this open source code, 
please support Adafruit and open-source hardware by purchasing 
products from Adafruit!

*/
/**************************************************************************/
#include <Wire.h>
#include <SPI.h>
#include <Adafruit_PN532.h>

// If using the breakout with SPI, define the pins for SPI communication.
#define PN532_SCK  (2)
#define PN532_MOSI (3)
#define PN532_SS   (4)
#define PN532_MISO (5)

// If using the breakout or shield with I2C, define just the pins connected
// to the IRQ and reset lines.  Use the values below (2, 3) for the shield!
//#define PN532_IRQ   (2)
//#define PN532_RESET (3)  // Not connected by default on the NFC Shield

// Uncomment just _one_ line below depending on how your breakout or shield
// is connected to the Arduino:

// Use this line for a breakout with a software SPI connection (recommended):
Adafruit_PN532 nfc(PN532_SCK, PN532_MISO, PN532_MOSI, PN532_SS);

// Use this line for a breakout with a hardware SPI connection.  Note that
// the PN532 SCK, MOSI, and MISO pins need to be connected to the Arduino's
// hardware SPI SCK, MOSI, and MISO pins.  On an Arduino Uno these are
// SCK = 13, MOSI = 11, MISO = 12.  The SS line can be any digital IO pin.
//Adafruit_PN532 nfc(PN532_SS);

// Or use this line for a breakout or shield with an I2C connection:
//Adafruit_PN532 nfc(PN532_IRQ, PN532_RESET);

#if defined(ARDUINO_ARCH_SAMD)
// for Zero, output on USB Serial console, remove line below if using programming port to program the Zero!
// also change #define in Adafruit_PN532.cpp library file
   #define Serial SerialUSB
#endif

/*---------------------------- Module Functions ---------------------------*/
/* prototypes for private functions for this service.They should be functions
   relevant to the behaviour of this service
*/



/*---------------------------- Module Variables ---------------------------*/
// with the introduction of Gen2, we need a module level Priority variable
static uint8_t MyPriority;
// add a deferral queue for up to 3 pending deferrals +1 to allow for overhead
static ES_Event DeferralQueue[3+1];

static uint32_t versiondata;

static bool RFID_Hold;

RFIDState_t CurrentRFIDState;

/*------------------------------ Module Code ------------------------------*/
/****************************************************************************
 Function
     InitRFIDService

 Parameters
     uint8_t : the priorty of this service

 Returns
     bool, false if error in initialization, true otherwise

 Description
     Saves away the priority, and does any 
     other required initialization for this service
 Notes

****************************************************************************/
bool InitRFIDService ( uint8_t Priority )
{
  ES_Event ThisEvent;
  MyPriority = Priority;
  /********************************************
   in here you write your initialization code
   *******************************************/
   //Serial.println("fffffffffffffffffffffffff");
   #ifndef ESP8266
    while (!Serial); // for Leonardo/Micro/Zero
  #endif

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
     PostRFIDService

 Parameters
     EF_Event ThisEvent ,the event to post to the queue

 Returns
     bool false if the Enqueue operation failed, true otherwise

 Description
     Posts an event to this state machine's queue
 Notes

****************************************************************************/
bool PostRFIDService( ES_Event ThisEvent )
{
  return ES_PostToService( MyPriority, ThisEvent);
}

/****************************************************************************
 Function
    RunRFIDService

 Parameters
   ES_Event : the event to process

 Returns
   ES_Event, ES_NO_EVENT if no error ES_ERROR otherwise

 Description
   add your description here
 Notes
   
****************************************************************************/
ES_Event RunRFIDService( ES_Event ThisEvent )
{
  //Serial.println("!!!!!!!!!!!!!");
  ES_Event ReturnEvent;
  ReturnEvent.EventType = ES_NO_EVENT; // assume no errors
  if (ThisEvent.EventType == ES_INIT){
    RFID_Hold = false;
    ES_Timer_InitTimer(RFID_CHECKING_TIMER, HALF_SEC);
    CurrentRFIDState = RFIDDisconnected;
  }
  if((ThisEvent.EventType == ES_TIMEOUT) && (ThisEvent.EventParam == RFID_CHECKING_TIMER)){
    switch(CurrentRFIDState){
      case RFIDDisconnected:
        nfc.begin();
        versiondata = nfc.getFirmwareVersion();
        if (! versiondata) {
          Serial.print("Didn't find PN53x board");
          ES_Timer_InitTimer(RFID_CHECKING_TIMER, FIVE_SEC);
          }
        else{
          // configure board to read RFID tags
          nfc.SAMConfig();
          Serial.println("Waiting for an ISO14443A Card ...");
          CurrentRFIDState = RFIDConnected;
          ES_Timer_InitTimer(RFID_CHECKING_TIMER, HALF_SEC);
          }
      break;
      case RFIDConnected:
        if(!RFID_Hold){
        //Serial.println("!!!!!!!!!!!!!");
        uint8_t success;
        uint8_t uid[] = { 0, 0, 0, 0, 0, 0, 0 };  // Buffer to store the returned UID
        uint8_t uidLength;                        // Length of the UID (4 or 7 bytes depending on ISO14443A card type)
        // Wait for an ISO14443A type cards (Mifare, etc.).  When one is found
        // 'uid' will be populated with the UID, and uidLength will indicate
        // if the uid is 4 bytes (Mifare Classic) or 7 bytes (Mifare Ultralight)
        success = nfc.readPassiveTargetID(PN532_MIFARE_ISO14443A, uid, &uidLength,200);
        //Serial.println("!!!!!!!!!!!!!");
        if (success) {
          // Display some basic information about the card
          //Serial.println("Found an ISO14443A card");
          //Serial.print("  UID Length: ");Serial.print(uidLength, DEC);Serial.println(" bytes");
          Serial.print("  UID Value: ");
          nfc.PrintHex(uid, uidLength);
          Serial.println("");
          ES_Event Event2Post;
          Event2Post.EventType = ES_RFID;

          if (uid[0] == 0x04 && uid[1] == 0x96 && uid[2] == 0xA5 && uid[3] == 0x89 && uid[4] == 0xBA && uid[5] == 0x4C && uid[6] == 0x76){
              Serial.println("This is Cloth No.1");
              Event2Post.EventParam = 1;
          }
          else if (uid[0] == 0x04 && uid[1] == 0x96 && uid[2] == 0xA5 && uid[3] == 0x89 && uid[4] == 0xBA && uid[5] == 0x48 && uid[6] == 0xD8){
              Serial.println("This is Cloth No.2");
              Event2Post.EventParam = 2;
          }
          else if (uid[0] == 0x9B && uid[1] == 0x2C && uid[2] == 0x7A && uid[3] == 0xDD){
              Serial.println("This is Cloth No.3");
              Event2Post.EventParam = 3;
          }
          
          
          
          PostLogicService(Event2Post);
          DeviceState_t CurrentDeviceState;
          CurrentDeviceState = QueryDeviceState();
          if((CurrentDeviceState == Wait4Scan)||(CurrentDeviceState == Wait4ScanAgain)){
            //Serial.println("!!!!!!!!!!!!");
            RFID_Hold = true;
            ES_Timer_InitTimer(RFID_HOLD_TIMER, FIVE_SEC);
          }
         }
        }
        ES_Timer_InitTimer(RFID_CHECKING_TIMER, HALF_SEC);
         
      break;
      
      }
    
    }
    else if((ThisEvent.EventType == ES_TIMEOUT) && (ThisEvent.EventParam == RFID_HOLD_TIMER)){
      RFID_Hold = false;
    }
  return ReturnEvent;
}

/***************************************************************************
 private functions
 ***************************************************************************/


/*------------------------------- Footnotes -------------------------------*/
/*------------------------------ End of file ------------------------------*/

