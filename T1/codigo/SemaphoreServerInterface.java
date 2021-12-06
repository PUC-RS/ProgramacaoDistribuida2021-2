import java.rmi.*;

public interface SemaphoreServerInterface extends Remote{
    public String P(int val) throws RemoteException, InterruptedException;
    public String V(int val) throws RemoteException, InterruptedException;
}

