# Creates a socket server and dumps the received data to the Arduino

from Adafruit_CharLCD import Adafruit_CharLCD
from subprocess import *
from time import sleep
import serial
import socket

#arduino=serial.Serial('/dev/ttyACM0', 9600)

server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind(("", 3005))
server_socket.listen(1)

print 'Socket started'
lcd = Adafruit_CharLCD()
lcd.begin(16,1)

while 1:

	lcd.clear()
	lcd.message('Awaiting client')
	client_socket, address = server_socket.accept()
	lcd.clear()
	lcd.message('Client connected')

	while 1:
		data = client_socket.recv(4096)
       	if data == 'quit' :
			break
		print data
		#arduino.write(data)
	    #arduino.flush()
