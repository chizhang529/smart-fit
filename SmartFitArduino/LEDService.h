/****************************************************************************
 
  Header file for LEDService
  based on the Gen 2 Events and Services Framework

 ****************************************************************************/

#ifndef LEDService_H
#define LEDService_H

#include "ES_Configure.h"
#include "./Framework/ES_Types.h"

// State definitions for use with the query function
typedef enum { Solid, Blink} LightMode_t ;
			   
// Public Function Prototypes

bool InitLEDService ( uint8_t Priority );
bool PostLEDService( ES_Event ThisEvent );
ES_Event RunLEDService( ES_Event ThisEvent );


#endif /* LEDService_H */

