import java.rmi.RemoteException;
import java.rmi.Remote;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;

public interface ServerInterface extends Remote
{
    String busca(final String p0, String nickname) throws MalformedURLException, RemoteException, NotBoundException, InterruptedException;
    String insere(final String p0, String nickname) throws MalformedURLException, RemoteException, NotBoundException, InterruptedException;
    String deleta(final String p0, String nickname) throws MalformedURLException, RemoteException, NotBoundException, InterruptedException;
}
