/****************************************************************************
 
  Header file for RFIDService
  based on the Gen 2 Events and Services Framework

 ****************************************************************************/

#ifndef RFIDService_H
#define RFIDService_H

#include "ES_Configure.h"
#include "./Framework/ES_Types.h"

// State definitions for use with the query function
typedef enum {RFIDDisconnected, RFIDConnected} RFIDState_t ;

// Public Function Prototypes

bool InitRFIDService ( uint8_t Priority );
bool PostRFIDService( ES_Event ThisEvent );
ES_Event RunRFIDService( ES_Event ThisEvent );


#endif /* RFIDService_H */

