#ifndef DEFINITION_H
#define DEFINITION_H


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

#endif /* DEFINITION_H */

