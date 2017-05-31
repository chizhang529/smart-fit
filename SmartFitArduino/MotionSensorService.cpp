/****************************************************************************
 Module
   MotionSensorService.c

 Revision
   1.0.1

 Description
   This is Motion Sensor Service under the 
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
#include "MotionSensorService.h"
#include "LogicService.h"


/*----------------------------- Module Defines ----------------------------*/
// these times assume a 10.24mS/tick timing
#define ONE_SEC 98
#define HALF_SEC (ONE_SEC/2)
#define QUARTER_SEC (ONE_SEC/4)
#define ONE_EIGHTH_SEC (ONE_SEC/8)
#define TWO_SEC (ONE_SEC*2)
#define FIVE_SEC (ONE_SEC*5)
#define ONE_MIN (ONE_SEC*60)

#define MotionSensor 7
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

static MotionSensorState_t CurrentState;
static bool LastMotionState;
/*------------------------------ Module Code ------------------------------*/
/****************************************************************************
 Function
     InitMotionSensorService

 Parameters
     uint8_t : the priorty of this service

 Returns
     bool, false if error in initialization, true otherwise

 Description
     Saves away the priority, and does any 
     other required initialization for this service
 Notes

****************************************************************************/
bool InitMotionSensorService ( uint8_t Priority )
{
  ES_Event ThisEvent;
  MyPriority = Priority;
  /********************************************
   in here you write your initialization code
   *******************************************/
  pinMode(MotionSensor, INPUT);
  LastMotionState = Motion_Not_Detected;
  CurrentState = NotDetected;
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
     PostMotionSensorService

 Parameters
     EF_Event ThisEvent ,the event to post to the queue

 Returns
     bool false if the Enqueue operation failed, true otherwise

 Description
     Posts an event to this state machine's queue
 Notes

****************************************************************************/
bool PostMotionSensorService( ES_Event ThisEvent )
{
  return ES_PostToService( MyPriority, ThisEvent);
}

/****************************************************************************
 Function
    RunMotionSensorService

 Parameters
   ES_Event : the event to process

 Returns
   ES_Event, ES_NO_EVENT if no error ES_ERROR otherwise

 Description
   add your description here
 Notes
   
****************************************************************************/
ES_Event RunMotionSensorService( ES_Event ThisEvent )
{
  ES_Event ReturnEvent;
  ReturnEvent.EventType = ES_NO_EVENT; // assume no errors

  switch(CurrentState)
	{
		case NotDetected:
      if (ThisEvent.EventType == ES_MOTION_SENSOR && ThisEvent.EventParam == Motion_Detected){
        Serial.println("Motion Sensor State Changed: Motion Detected");
        CurrentState = Detected;
       }
		break;

    case Detected:
      if (ThisEvent.EventType == ES_MOTION_SENSOR && ThisEvent.EventParam == Motion_Not_Detected){
        Serial.println("Motion Sensor State Changed: Seems no one, start timer");
        CurrentState = Sampling;
        ES_Timer_InitTimer(MOTION_SENSOR_TIMER, ONE_MIN);
        }
	      
    break;

    case Sampling:
      if (ThisEvent.EventType == ES_MOTION_SENSOR && ThisEvent.EventParam == Motion_Detected){
        Serial.println("Motion Sensor State Changed: Motion Detected, stop timer");
        CurrentState = Detected;
        ES_Timer_StopTimer(MOTION_SENSOR_TIMER);
       }
      if (ThisEvent.EventType == ES_TIMEOUT && ThisEvent.EventParam == MOTION_SENSOR_TIMER){
        Serial.println("Motion Sensor State Changed: Motion Not Detected. RESTART EVERYTHING");
        CurrentState = NotDetected;
        ES_Event Event2Post;
        Event2Post.EventType = ES_MOTION_SENSOR;
        Event2Post.EventParam = Motion_Not_Detected;
        PostLogicService(Event2Post);
       }
        
        
    break;
		default:
		
		break;
	}
  return ReturnEvent;
}



bool Check4Motion ( void ){
  pinMode(MotionSensor, INPUT); 
  bool MotionState;
  if (digitalRead(MotionSensor) == HIGH){
    MotionState = Motion_Detected;
    //Serial.println("Motion is Detected");
  }
  else {
    MotionState = Motion_Not_Detected;
    //Serial.println("Motion is not Detected");
  }
 
  if (MotionState != LastMotionState){
    ES_Event Event2Post;
    Event2Post.EventType = ES_MOTION_SENSOR;
    if (MotionState == Motion_Detected){
      //Serial.println("Motion Sensor State Changed: Motion Detected");
      Event2Post.EventParam = Motion_Detected;
    }
    else{
      //Serial.println("Motion Sensor State Changed: Motion Not Detected");
      Event2Post.EventParam = Motion_Not_Detected;
    }
    PostMotionSensorService(Event2Post);
    LastMotionState = MotionState;
    return true;
  }
 else{
    return false;
  }
}
/***************************************************************************
 private functions
 ***************************************************************************/

/*------------------------------- Footnotes -------------------------------*/
/*------------------------------ End of file ------------------------------*/

