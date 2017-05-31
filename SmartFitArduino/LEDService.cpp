/****************************************************************************
 Module
   LEDService.c

 Revision
   1.0.1

 Description
   This is LED Service under the 
   Gen2 Events and Services Framework.

 Notes
  Key 'n': turn on LED
  Key 'f': turn off LED (default)
  Key 'b': LED in blinking mode
  Key 's': LED in solid mode

 History

****************************************************************************/
/*----------------------------- Include Files -----------------------------*/
/* include header files for the framework and this service
*/
#include "ES_Configure.h"
#include "./Framework/ES_Framework.h"
#include "./Framework/ES_DeferRecall.h"
#include "LEDService.h"


/*----------------------------- Module Defines ----------------------------*/
// these times assume a 10.24mS/tick timing
#define ONE_SEC 98
#define HALF_SEC (ONE_SEC/2)
#define QUARTER_SEC (ONE_SEC/4)
#define ONE_EIGHTH_SEC (ONE_SEC/8)
#define TWO_SEC (ONE_SEC*2)
#define FIVE_SEC (ONE_SEC*5)

#define command_on 0
#define command_off 1
#define command_blink 2
#define command_solid 3

#define blueLED1 6
#define blueLED2 6
#define on 1
#define off 0
/*---------------------------- Module Functions ---------------------------*/
/* prototypes for private functions for this service.They should be functions
   relevant to the behaviour of this service
*/

static void initLED(void);
static void turnOnLED(void);
static void turnOffLED(void);

/*---------------------------- Module Variables ---------------------------*/
// with the introduction of Gen2, we need a module level Priority variable
static uint8_t MyPriority;
// add a deferral queue for up to 3 pending deferrals +1 to allow for overhead
static ES_Event DeferralQueue[3+1];
// a bool variable indicating if it is on or not (1: on; 0: off)
static bool lightState;
// a bool variable indicating if LED is lit or not
static bool lit;
// intensity (0 - 255, but 0 - 100 makes light brightness vary a lot)
static int intensity;
// Current LightState
static LightMode_t CurrentLightMode;
/*------------------------------ Module Code ------------------------------*/
/****************************************************************************
 Function
     InitLEDService

 Parameters
     uint8_t : the priorty of this service

 Returns
     bool, false if error in initialization, true otherwise

 Description
     Saves away the priority, and does any 
     other required initialization for this service
 Notes

****************************************************************************/
bool InitLEDService ( uint8_t Priority )
{
  ES_Event ThisEvent;
  MyPriority = Priority;
  /********************************************
   in here you write your initialization code
   *******************************************/
   // initialize LED drive for testing/debug output
   initLED();
   // Set current light mode to solid
   CurrentLightMode = Solid;
   // Set intensity to 100 as default value
   intensity = 100;


   //Serial.println("!oooooooooooooo");

   
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
     PostLEDService

 Parameters
     EF_Event ThisEvent ,the event to post to the queue

 Returns
     bool false if the Enqueue operation failed, true otherwise

 Description
     Posts an event to this state machine's queue
 Notes

****************************************************************************/
bool PostLEDService( ES_Event ThisEvent )
{
  return ES_PostToService( MyPriority, ThisEvent);
}

/****************************************************************************
 Function
    RunLEDService

 Parameters
   ES_Event : the event to process

 Returns
   ES_Event, ES_NO_EVENT if no error ES_ERROR otherwise

 Description
   add your description here
 Notes
   
****************************************************************************/
ES_Event RunLEDService( ES_Event ThisEvent )
{
  ES_Event ReturnEvent;
  ReturnEvent.EventType = ES_NO_EVENT; // assume no errors

  switch(CurrentLightMode)
	{
		case Solid:
			if ((ThisEvent.EventType == ES_NEW_KEY && ThisEvent.EventParam == 'n' )||(ThisEvent.EventType == ES_LED_COMMAND && ThisEvent.EventParam == command_on)){
				turnOnLED();
				//Serial.println(F("Turn on LED. Mode: Solid"));
			}
			if ((ThisEvent.EventType == ES_NEW_KEY && ThisEvent.EventParam == 'f' )||(ThisEvent.EventType == ES_LED_COMMAND && ThisEvent.EventParam == command_off)){
				turnOffLED();
				//Serial.println(F("Turn off LED. Mode: Solid"));
			}
			if ((ThisEvent.EventType == ES_NEW_KEY && ThisEvent.EventParam == 'b' )||(ThisEvent.EventType == ES_LED_COMMAND && ThisEvent.EventParam == command_blink)){
				CurrentLightMode = Blink;
				if (lightState == on){
					ES_Timer_InitTimer(BLINK_TIMER, ONE_EIGHTH_SEC);
				}
				//Serial.println(F("Switch Mode to: Blink"));
			}
     /*
     if (ThisEvent.EventType == ES_LIGHT_SENSOR && ThisEvent.EventParam == sensor_covered ){
       CurrentLightMode = Blink;
        if (lightState == on){
          ES_Timer_InitTimer(BLINK_TIMER, ONE_EIGHTH_SEC);
        }
        Serial.println(F("Switch Mode to: Blink"));
      }
      */
		break;

    case Blink:
	        if (ThisEvent.EventType == ES_TIMEOUT && ThisEvent.EventParam == BLINK_TIMER && lightState == on){
				if (lit == true) {
					turnOffLED();
          lightState = on;
          //Serial.println(F("Blink LED. Turned off"));
				}
				else if (lit == false){
					turnOnLED();
          //Serial.println(F("Blink LED. Turned on"));
				}
				ES_Timer_InitTimer(BLINK_TIMER, ONE_EIGHTH_SEC);
			}
			if ((ThisEvent.EventType == ES_NEW_KEY && ThisEvent.EventParam == 'n' )||(ThisEvent.EventType == ES_LED_COMMAND && ThisEvent.EventParam == command_on)){
				turnOnLED();
				ES_Timer_InitTimer(BLINK_TIMER, ONE_EIGHTH_SEC);
				//Serial.println(F("Turn on LED. Mode: Blink"));
			}
			if ((ThisEvent.EventType == ES_NEW_KEY && ThisEvent.EventParam == 'f' )||(ThisEvent.EventType == ES_LED_COMMAND && ThisEvent.EventParam == command_off)){
				turnOffLED();
				//Serial.println(F("Turn off LED. Mode: Blink"));
			}
     if ((ThisEvent.EventType == ES_NEW_KEY && ThisEvent.EventParam == 's' )||(ThisEvent.EventType == ES_LED_COMMAND && ThisEvent.EventParam == command_solid)){
        CurrentLightMode = Solid;
        if (lightState == on){
          turnOnLED();
        }
        else if (lightState == off){
          turnOffLED();
        }
        //Serial.println(F("Switch Mode to: Solid"));
      }
      /*
      if (ThisEvent.EventType == ES_LIGHT_SENSOR && ThisEvent.EventParam == sensor_not_covered ){
        CurrentLightMode = Solid;
        if (lightState == on){
          turnOnLED();
        }
        else if (lightState == off){
          turnOffLED();
        }
        Serial.println(F("Switch Mode to: Solid"));
      }
      */
    break;
		default:
		
		break;
	}
  return ReturnEvent;
}

/***************************************************************************
 private functions
 ***************************************************************************/


static void initLED(void)
{
	// Set pin 10, 11 as digital output
	pinMode(blueLED1,OUTPUT);
    pinMode(blueLED2,OUTPUT);
	// Set internal LED (pin 13) as an indication
	pinMode(13,OUTPUT);
    //Serial.println(F("LED pins initialized"));
	// Turn off all of the LEDs
	turnOffLED();
}

static void turnOnLED(void)
{
	// PWM output to pin 10, 11, with intensity from 0 to 100 (although 0 to 255 is a valid range)
	analogWrite(blueLED1,intensity);
	analogWrite(blueLED2,intensity);
	// Turn on internal LED as an indication
	digitalWrite(13,HIGH);
	// Set lightState to on
	lightState = on;
	// Set lit flag to true
	lit = true;
  //Serial.println(F("LED turned on"));
}

static void turnOffLED(void)
{
	// PWM output to pin 10, 11, with intensity as 0
	analogWrite(blueLED1,0);
	analogWrite(blueLED2,0);
	// Turn off internal LED as an indication
	digitalWrite(13,LOW);
	// Set lightState to off
	lightState = off;
	// Set lit flag to false
	lit = false;
 //Serial.println(F("LED turned off"));
}
/*------------------------------- Footnotes -------------------------------*/
/*------------------------------ End of file ------------------------------*/

