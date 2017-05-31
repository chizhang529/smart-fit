/****************************************************************************
 
  Header file for LogicService
  based on the Gen 2 Events and Services Framework

 ****************************************************************************/

#ifndef LogicService_H
#define LogicService_H

#include "ES_Configure.h"
#include "./Framework/ES_Types.h"

// State definitions for use with the query function
typedef enum {Wait4Start, Wait4Scan, Wait4LoseConnection, RunningProductPage, Wait4ScanAgain} DeviceState_t ;
			   
// Public Function Prototypes

bool InitLogicService( uint8_t Priority );
bool PostLogicService( ES_Event ThisEvent );
ES_Event RunLogicService( ES_Event ThisEvent );
DeviceState_t QueryDeviceState(void);

#endif /* LogicService_H */

