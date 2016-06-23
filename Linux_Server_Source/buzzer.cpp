#include <stdio.h>
#include <stdlib.h>   
#include <unistd.h>
#include <iostream>
#include <wiringPi.h>

using namespace std;

#define PIN_OUTPUT	(12)			// GPIO 10
#define PIN_INPUT	(4)				// GPIO 23

int main(int ac, char *av[])
{
	bool isOpening = false;

	if(-1 == wiringPiSetup())
		return -1;
	pinMode(PIN_OUTPUT, OUTPUT);
	pinMode(PIN_INPUT, INPUT);

	cout<<"BUZZER is working"<<endl;

	while(1)
	{
		sleep(1);
		if(digitalRead(PIN_INPUT))
		{
			cout<<"close buzzer"<<endl;
			digitalWrite(PIN_OUTPUT, 0);
			isOpening = false;
		}
		else
		{
			if(!isOpening)
				sleep(10);
			if(!digitalRead(PIN_INPUT))
			{
				isOpening = true;
				digitalWrite(PIN_OUTPUT, 1);
			}
		}
	}

	return 0;
}
