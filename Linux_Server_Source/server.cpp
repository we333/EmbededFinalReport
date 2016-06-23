#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <time.h>
#include <string.h>
#include <iostream>
#include <wiringPi.h>

using namespace std;

#define Err(x)	{perror(x);}
#define PORTNUM	(14000)
#define HOSTLEN	(256)

#define PIN_OUTPUT	(12)			// GPIO 10
#define PIN_INPUT	(4)				// GPIO 23

void sanitize(char *);
void door_monitor();

int main(int ac, char *av[])
{
	struct sockaddr_in saddr;
	struct hostent *hp;
	char hostname[HOSTLEN];
	int sock_id, sock_fd;
	FILE *sock_fpi, *sock_fpo;
	FILE *pipe_fp;
	char dir[BUFSIZ], cmd[BUFSIZ];
	int dirlen, c;
	int pid;

	char buzzer_action[] = "sudo ./buzzer";
	char on[] = "sudo ./ledtest 0 1";
	char off[] = "sudo ./ledtest 0 0";

	if(-1 == wiringPiSetup())
		Err("wiringPi Setup failed");
	pinMode(PIN_OUTPUT, OUTPUT);
	pinMode(PIN_INPUT, INPUT);

	if(-1 == (sock_id = socket(PF_INET, SOCK_STREAM, 0)))
		Err("socket failed");

	bzero((void *)&saddr, sizeof(saddr));
	if(NULL == (hp = gethostbyname("192.168.0.104")))
		Err("get ip by name failed");				
	bcopy((void *)hp->h_addr, (void *)&saddr.sin_addr, hp->h_length);
	saddr.sin_port = htons(PORTNUM);	// transfer to internet byte order: big-endian
	saddr.sin_family = AF_INET;

	if(0 != bind(sock_id, (struct sockaddr *)&saddr, sizeof(saddr)))
		Err("bind failed");
	if(0 != listen(sock_id, 1))
		Err("listen failed");

	// call buzzer for loop monitor
	popen(buzzer_action, "r");

	while(1)
	{
		if(-1 == (sock_fd = accept(sock_id, NULL, NULL)))
			Err("accept failed");
		cout<<"calls on socket!!"<<endl;

		// read info from Android
		if(NULL == (sock_fpi = fdopen(sock_fd, "r")))
			Err("fdopen reading failed");
		if(NULL == fgets(dir, BUFSIZ-5, sock_fpi))
			Err("reading dirname failed");
		cout<<"dir -- "<<dir<<endl;

		// write info to Android
		if(NULL == (sock_fpo = fdopen(sock_fd, "w")))
			Err("fdopen writing failed");

		sprintf(cmd, "%s", dir);
		/*	20160624 we
			if do not call popen(), server will close after called by client
		*/
		if(NULL == (pipe_fp = popen(cmd, "r")))
			Err("popen failed");

	 	if(dir[0] == 'A')
	 		digitalWrite(PIN_OUTPUT, 1);
	 	else if(dir[0] == 'B')
	 		digitalWrite(PIN_OUTPUT, 0);
	 	else if(dir[0] == 'C')
	 	{
	 		c = digitalRead(PIN_INPUT) ? 'T' : 'F';
	 		putc(c, sock_fpo);
			putc('\n', sock_fpo);
	 	}
	 	else
	 		cout<<"VVVV"<<endl;

		pclose(pipe_fp);
		fclose(sock_fpo);
		fclose(sock_fpi);
	}
	
	return 0;
}
/*
	if someone send an dirname like "; rm *",
	we naively created a command "ls; rm *"
*/
void sanitize(char *str)
{
	char *src, *dest;
	for(src = dest = str; *src; src++)
		if(*src == '/' || isalnum(*src))
			*dest++ = *src;
	*dest = '\0';
}
