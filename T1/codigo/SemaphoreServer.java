import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.util.*;
import java.util.concurrent.*;

public class SemaphoreServer extends UnicastRemoteObject implements SemaphoreServerInterface{
	private static final long serialVersionUID = 1L;
	private int permissionValue = 10;

	public SemaphoreServer() throws RemoteException {
		super();
    }
	
	public static void main(String[] args) throws RemoteException {
		if (args.length != 2) {
			System.out.println("Usage: java SemaphoreServer <server ip> <server port>");
			System.exit(1);
		}

		String server = "rmi://" + args[0] + ":" + args[1] + "/semaphore";

		try {
			System.setProperty("java.rmi.server.hostname", args[0]);
			LocateRegistry.createRegistry(Integer.parseInt(args[1]));
			System.out.println("java RMI registry created at: " + args[0] + ":" + args[1]);
		} catch (RemoteException e) {
			System.out.println("java RMI registry already exists.");
		}

		try {
			Naming.rebind(server, new SemaphoreServer());
			System.out.println("Server is ready.");
		} catch (Exception e) {
			System.out.println("Serverfailed: " + e);
		}
	}

	public synchronized String P(int val) throws RemoteException, InterruptedException{
		//permissionValue == 10, Exclui: val == 10,  Insere: val == 8, Busca: val == 0

		while (permissionValue < val) { 
			this.wait(); //Aguarda até ocorrer um notifyAll 
		}
		permissionValue -= val; //Decrementa permissionValue para reservar acesso, desta forma qualquer outra chamada fica bloqueada no while acima
		return "Acesso à uma região crítica iniciado";
	}

	public synchronized String V(int val) throws RemoteException, InterruptedException{
		permissionValue += val; //Incrementa permissionValue liberando o acesso para outros processos
		this.notifyAll();
		return "Acesso à uma região crítica finalizado";
	}
	
}