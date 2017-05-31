#include "./Framework/ES_Port.h"
#include "./Framework/ES_Framework.h"

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  Serial.print(F("Last Compile at "));
  Serial.print(F(__TIME__));
  Serial.print(F( " on "));
  Serial.println(F( __DATE__));
  
  ES_Initialize(ES_Timer_RATE_1mS);
}

void loop() {
  // put your main code here, to run repeatedly:
  ES_Run();
}
