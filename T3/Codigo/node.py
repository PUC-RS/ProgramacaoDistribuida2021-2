import sys
from timeit import default_timer as timer
from dateutil import parser
import threading
import datetime
import socket
import time

def mandaClock(client):
	while True:
		client.send(str(datetime.datetime.now()).encode())

		print("Clock enviado com sucesso", end = "\n\n")
		time.sleep(int(sys.argv[5]))

def recebeClock(client):
	while True:
		clock = parser.parse(client.recv(1024).decode())

		print("Clock no cliente é: " + str(clock), end = "\n\n")

def inicialClockNodo(host, port):
	client = socket.socket()		
		
	client.connect((host, port))

	mandaClockThread = threading.Thread(target = mandaClock, args = (client, ))
	mandaClockThread.start()

	recebeClockThread = threading.Thread(target = recebeClock, args = (client, ))
	recebeClockThread.start()