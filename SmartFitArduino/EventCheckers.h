/****************************************************************************
 Module
     EventCheckers.h
 Description
     header file for the event checking functions
 Notes

 History
 When           Who     What/Why
 -------------- ---     --------
 10/18/15 11:50 jec      added #include for stdint & stdbool
 08/06/13 14:37 jec      started coding
*****************************************************************************/

#ifndef EventCheckers_H
#define EventCheckers_H

// the common headers for C99 types 
#include <stdint.h>
#include <stdbool.h>

// If you have event checkers that reside in service modules so that they can  
// be initialized at startup, then #include the headers for those services here

// prototypes for the event checkers in EventCheckers.cpp

bool Check4Keystroke(void);
bool CheckforMotion(void);


#endif /* EventCheckers_H */
