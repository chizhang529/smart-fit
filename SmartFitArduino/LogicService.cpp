/****************************************************************************
 Module
   LogicService.c

 Revision
   1.0.1

 Description
   This is Logic Service under the 
   Gen2 Events and Services Framework.

 Notes

****************************************************************************/
/*----------------------------- Include Files -----------------------------*/
/* include header files for the framework and this service
*/
#include "ES_Configure.h"
#include "./Framework/ES_Framework.h"
#include "./Framework/ES_DeferRecall.h"
#include "LogicService.h"
#include "LEDService.h"
#include "BluetoothService.h"
#include "RFIDService.h"
#include "MotionSensorService.h"

/*----------------------------- Module Defines ----------------------------*/
// these times assume a 10.24mS/tick timing
#define ONE_SEC 98
#define HALF_SEC (ONE_SEC/2)
#define QUARTER_SEC (ONE_SEC/4)
#define ONE_EIGHTH_SEC (ONE_SEC/8)
#define TWO_SEC (ONE_SEC*2)
#define FIVE_SEC (ONE_SEC*5)

// following definations are from BluetoothService
#define BLE_DISCONNECT 0
#define BLE_CONNECT 1
#define RX_START 2 //Received from tablet that customer should start scanning tag
#define TX_END 3 //Transmit to tablet that fitting room door is opened and restart app
#define TX_RFID 4 //Transmit to tablet the UID of RFID tag scanned

// following definations are from LEDService
#define command_on 0
#define command_off 1
#define command_blink 2
#define command_solid 3

// following definations are from LogicService
#define accel_moving 1
#define accel_not_moving 0

// following definitions are from MotionSensorService
#define Motion_Detected 1
#define Motion_Not_Detected 0


/*---------------------------- Module Functions ---------------------------*/
/* prototypes for private functions for this service.They should be functions
   relevant to the behaviour of this service
*/


/*---------------------------- Module Variables ---------------------------*/
// with the introduction of Gen2, we need a module level Priority variable
static uint8_t MyPriority;
// add a deferral queue for up to 3 pending deferrals +1 to allow for overhead
static ES_Event DeferralQueue[3+1];

static DeviceState_t DeviceState;

// define variables for connection, lightState, lightMode, lightSensorState, accelState

/*------------------------------ Module Code ------------------------------*/
/****************************************************************************
 Function
     InitLogicService

 Parameters
     uint8_t : the priorty of this service

 Returns
     bool, false if error in initialization, true otherwise

 Description
     Saves away the priority, and does any 
     other required initialization for this service
 Notes

****************************************************************************/
bool InitLogicService ( uint8_t Priority )
{
  ES_Event ThisEvent;
  MyPriority = Priority;
  /********************************************
   in here you write your initialization code
   *******************************************/
  // Set DeviceState to Wait4BLEConnect
  DeviceState = Wait4Start;
  // post the initial transition event
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
     PostLogicService

 Parameters
     EF_Event ThisEvent ,the event to post to the queue

 Returns
     bool false if the Enqueue operation failed, true otherwise

 Description
     Posts an event to this state machine's queue
 Notes

****************************************************************************/
bool PostLogicService( ES_Event ThisEvent )
{
  return ES_PostToService( MyPriority, ThisEvent);
}

/****************************************************************************
 Function
    RunLogicService

 Parameters
   ES_Event : the event to process

 Returns
   ES_Event, ES_NO_EVENT if no error ES_ERROR otherwise

 Description
   add your description here
 Notes
   
****************************************************************************/
ES_Event RunLogicService( ES_Event ThisEvent )
{
  ES_Event ReturnEvent;
  ReturnEvent.EventType = ES_NO_EVENT; // assume no errors
  
  if (ThisEvent.EventType == ES_MOTION_SENSOR && ThisEvent.EventParam == Motion_Not_Detected){
      ES_Event Event2Post;
      Event2Post.EventType = ES_BLE_COMMAND;
      Event2Post.EventParam = TX_END;
      ES_Timer_InitTimer(RESET_TIMER, ONE_SEC);
      PostBluetoothService(Event2Post); 
  }
  
   if (ThisEvent.EventType == ES_TIMEOUT && ThisEvent.EventParam == RESET_TIMER){
      DeviceState = Wait4Start;
      InitLEDService(0);
      InitBluetoothService(1);
      InitRFIDService(2);
      InitMotionSensorService(4);
      Serial.println("Logic Service: There is nobody so reset everything");
  }
  
  //Serial.println("Logic Service receives an event");
  
  switch (DeviceState){
    case Wait4Start:
      if(ThisEvent.EventType == ES_BLE_STATE && ThisEvent.EventParam == BLE_CONNECT){
        Serial.println("LogicService: Go to Wait4Scan State");
        DeviceState = Wait4Scan;
        ES_Event Event2Post;
        Event2Post.EventType = ES_LED_COMMAND;
        Event2Post.EventParam = command_blink;
        PostLEDService(Event2Post);
        Event2Post.EventParam = command_on;
        PostLEDService(Event2Post);
        }
    break;

    case Wait4Scan:
      if((ThisEvent.EventType == ES_RFID /*&& ThisEvent.EventParam == RFID number*/)/*||(ThisEvent.EventType == ES_NEW_KEY && ThisEvent.EventParam == 'r')*/){
        Serial.println("LogicService: Go to Wait4LoseConnection");
        DeviceState = Wait4LoseConnection;
        ES_Event Event2Post;
        Event2Post.EventType = ES_RFID;
        Event2Post.EventParam = ThisEvent.EventParam;
        PostBluetoothService(Event2Post);
        Event2Post.EventType = ES_LED_COMMAND;
        Event2Post.EventParam = command_solid;
        PostLEDService(Event2Post);
        }
     break;

     case Wait4LoseConnection:
       if (ThisEvent.EventType == ES_BLE_STATE && ThisEvent.EventParam == BLE_DISCONNECT){
        Serial.println("LogicService: Go to RunningProductPage");
        DeviceState = RunningProductPage;
        }
      break;

      case RunningProductPage:
       if ((ThisEvent.EventType == ES_BLE_STATE && ThisEvent.EventParam == BLE_CONNECT)||(ThisEvent.EventType == ES_BLE_COMMAND && ThisEvent.EventParam == RX_START)){
        Serial.println("LogicService: Go to Wait4ScanAgain");
        DeviceState = Wait4ScanAgain;
        ES_Event Event2Post;
        Event2Post.EventType = ES_LED_COMMAND;
        Event2Post.EventParam = command_blink;
        PostLEDService(Event2Post);
        }
      break;

      case Wait4ScanAgain:
        if((ThisEvent.EventType == ES_RFID /*&& ThisEvent.EventParam == RFID number*/)/*||(ThisEvent.EventType == ES_NEW_KEY && ThisEvent.EventParam == 'r')*/){
        Serial.println("LogicService: Go back to RunningProductPage");
        DeviceState = RunningProductPage;
        ES_Event Event2Post;
        Event2Post.EventType = ES_RFID;
        Event2Post.EventParam = ThisEvent.EventParam;
        PostBluetoothService(Event2Post);
        Event2Post.EventType = ES_LED_COMMAND;
        Event2Post.EventParam = command_solid;
        PostLEDService(Event2Post);
        }
      break;
  }
  
  return ReturnEvent;
}

DeviceState_t QueryDeviceState(void){
  return DeviceState;
  }
/***************************************************************************
 private functions
 ***************************************************************************/



/*------------------------------- Footnotes -------------------------------*/
/*------------------------------ End of file ------------------------------*/

