import sys
import master
import node
import datetime

if __name__ == '__main__':
    if len(sys.argv) != 6:
        print(
            "Argumentos invalidos, use: python main.py <id> <host> <port> <time> <delay>\n")
        sys.exit('')
    elif sys.argv[1] == '0':
        while int(sys.argv[4]) != int(datetime.datetime.now().minute):
            pass
        print("Rodando nodo MASTER\n")
        master.iniciaClockMaster(sys.argv[2], int(sys.argv[3]))
    else:
        while int(sys.argv[4]) != int(datetime.datetime.now().minute):
            pass
        print("Rodando cliente NODO\n")
        node.inicialClockNodo(sys.argv[2], int(sys.argv[3]))
