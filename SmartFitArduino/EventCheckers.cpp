/****************************************************************************
 Module
   EventCheckers.c

 Revision
   1.0.1 

 Description
   This is the sample for writing event checkers along with the event 
   checkers used in the basic framework test harness.

 Notes
   Note the use of static variables in sample event checker to detect
   ONLY transitions.
   
 History
 When           Who     What/Why
 -------------- ---     --------
 08/06/13 13:36 jec     initial version
****************************************************************************/

// this will pull in the symbolic definitions for events, which we will want
// to post in response to detecting events
#include "ES_Configure.h"
// this will get us the structure definition for events, which we will need
// in order to post events in response to detecting events
#include "./Framework/ES_Events.h"
// if you want to use distribution lists then you need those function 
// definitions too.
#include "./Framework/ES_PostList.h"

// This include will pull in all of the headers from the service modules
// providing the prototypes for all of the post functions
//#include "./Framework/ES_ServiceHeaders.h"
#include "LEDService.h"
#include "LogicService.h"
#include "MotionSensorService.h"

// this test harness for the framework references the serial routines that
// are defined in ES_Port.c
#include "./Framework/ES_Port.h"
// include our own prototypes to insure consistency between header & 
// actual functionsdefinition
#include "EventCheckers.h"


/****************************************************************************
 Function
   Check4Keystroke
 Parameters
   None
 Returns
   bool: true if a new key was detected & posted
 Description
   checks to see if a new key from the keyboard is detected and, if so, 
   retrieves the key and posts an ES_NewKey event to TestHarnessService0
 Notes
   The functions that actually check the serial hardware for characters
   and retrieve them are assumed to be in ES_Port.c
   Since we always retrieve the keystroke when we detect it, thus clearing the
   hardware flag that indicates that a new key is ready this event checker 
   will only generate events on the arrival of new characters, even though we
   do not internally keep track of the last keystroke that we retrieved.
 Author
   J. Edward Carryer, 08/06/13, 13:48
****************************************************************************/
bool Check4Keystroke(void)
{
  if ( IsNewKeyReady() ) // new key waiting?
  {
    ES_Event ThisEvent;
    ThisEvent.EventType = ES_NEW_KEY;
    ThisEvent.EventParam = GetNewKey();
    PostLEDService( ThisEvent );
    PostLogicService( ThisEvent );
    return true;
  }
  return false;
}

/****************************************************************************
 Function
   Check4Motion
 Parameters
   None
 Returns
   bool: true if there is a change in motion
****************************************************************************/
bool CheckforMotion(void){
  return Check4Motion();
}
