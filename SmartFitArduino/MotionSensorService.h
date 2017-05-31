/****************************************************************************
 
  Header file for MotionSensorService
  based on the Gen 2 Events and Services Framework

 ****************************************************************************/

#ifndef MotionSensorService_H
#define MotionSensorService_H

#include "ES_Configure.h"
#include "./Framework/ES_Types.h"

// State definitions for use with the query function
typedef enum {Detected, NotDetected, Sampling} MotionSensorState_t ;
			   
// Public Function Prototypes

bool InitMotionSensorService ( uint8_t Priority );
bool PostMotionSensorService( ES_Event ThisEvent );
ES_Event RunMotionSensorService( ES_Event ThisEvent );

bool Check4Motion ( void );

#endif /* MotionSensorService_H */

