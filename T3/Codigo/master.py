from functools import reduce
from dateutil import parser
import threading
import datetime
import socket
import time
import sys

clientData = {}

def recebeClockTime(conn, address):
    while True:
        clockData = conn.recv(1024).decode()
        clockTime = parser.parse(clockData)
        clockDiff = datetime.datetime.now() - clockTime

        clientData[address] = {
            "clockTime": clockTime,
            "time_difference": clockDiff,
            "connection": conn
        }

        print("Cliente atualizado com: " + str(address), end="\n\n")
        time.sleep(int(sys.argv[5]))


def fazConexao(master):
    while True:
        conn, addr = master.accept()
        clientAddr = str(addr[0]) + ":" + str(addr[1])

        print(clientAddr + " se conectou")

        current_thread = threading.Thread(
            target=recebeClockTime, args=(conn, clientAddr, ))
        current_thread.start()


def mediaDiferencaClock():
    clientDataCopy = clientData.copy()

    listaDiferencas = list(client['time_difference']
                           for clientAddr, client
                           in clientData.items())

    somaDiferencas = sum(listaDiferencas, datetime.timedelta(0, 0))

    media = somaDiferencas / len(clientData)

    return media


def sincronizaClocks():
    while True:
        print("Clientes sincronizados: " + str(len(clientData)))
        if len(clientData) > 0:
            media = mediaDiferencaClock()
            for clientAddr, client in clientData.items():
                try:
                    tempoSincronizado = datetime.datetime.now() + media
                    client['connection'].send(str(tempoSincronizado).encode())

                except Exception as e:
                    print("Aconteceu algo de errado em: " + str(clientAddr))

        else:
            print("Sincronizacao nao aplicavel. Cliente sem dados")
        print("\n\n")

        time.sleep(int(sys.argv[5]))


def iniciaClockMaster(host, port):

    master = socket.socket()
    master.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    master.bind((host, port))

    master.listen(10)

    masterThread = threading.Thread(target=fazConexao, args=(master, ))
    masterThread.start()

    syncThread = threading.Thread(target=sincronizaClocks, args=())
    syncThread.start()
