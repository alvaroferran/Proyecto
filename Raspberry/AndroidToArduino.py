# TCP server example
import socket
import serial

arduino=serial.Serial('/dev/ttyACM0', 9600)

server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind(("", 3005))
server_socket.listen(1)

while 1:
	client_socket, address = server_socket.accept()
	#print 'I got a connection from '+ address[0]

	#incoming= client_socket.recv(4096)
	#if incoming == 'Connect' :
	#    print incoming
	#    client_socket.send('Connected')
	#    print 'ok'

	while 1:
	    data = client_socket.recv(4096)
	    if data == 'quit' :
	    	break
	    print data
	    arduino.write(data)
	    arduino.flush()
	    
