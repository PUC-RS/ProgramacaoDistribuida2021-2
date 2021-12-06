import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.rmi.Remote;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.nio.charset.StandardCharsets;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;

public class Server extends UnicastRemoteObject implements ServerInterface{
    private static volatile String remoteHostName;
    private volatile List<String> database;
    private static SemaphoreServerInterface semaphoreServer;

    public Server() throws RemoteException {
        try {
            File f = new File("database.txt");
            if (f.createNewFile()) {
                System.out.println("File created: " + f.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public String busca(final String str, String nickname) 
        throws MalformedURLException, RemoteException, NotBoundException, InterruptedException {
        
        int queryValue = 0;
        System.out.println("\n" + nickname + " - Solicitou acesso para uma busca");
        String response = semaphoreServer.P(queryValue);
        System.out.println(nickname + " - Realizando operação de busca");

        List<String> conteudo = new ArrayList<String>();

        try{
            conteudo = Files.readAllLines(Paths.get("database.txt"), StandardCharsets.UTF_8);
            TimeUnit.SECONDS.sleep(3);
        } catch(IOException e) {
            e.printStackTrace();
        }

        int i = conteudo.indexOf(str);

        System.out.println(nickname + " - Busca realizada com sucesso");
        response = semaphoreServer.V(queryValue);

        if (i == -1){
            return str + " Não existe no banco de dados";
        }

        return "Operação de busca finalizada";
    }

    public String insere(final String str, String nickname) 
        throws MalformedURLException, RemoteException, NotBoundException, InterruptedException {
        
        int queryValue = 8;
        System.out.println("\n" + nickname + " - Solicitou acesso para uma insercao");
        String response = semaphoreServer.P(queryValue);
        System.out.println(nickname + " - Realizando operação de insercao");

         try {

            BufferedWriter writer = new BufferedWriter(new FileWriter("database.txt", true));

            writer.write(str+"\n");

            writer.close();

            TimeUnit.SECONDS.sleep(3);

            System.out.println(nickname + " - Insercao realizada com sucesso");

            response = semaphoreServer.V(queryValue);
        }catch (IOException e) {
            e.printStackTrace();
        }

        return "Operação de inserção finalizada";
    }

    public String deleta(final String str, String nickname) 
        throws MalformedURLException, RemoteException, NotBoundException, InterruptedException {
        
        int queryValue = 10;
        System.out.println("\n" + nickname + " - Solicitou acesso para uma exclusao");
        String response = semaphoreServer.P(queryValue);
        System.out.println(nickname + " - Realizando operação de exclusao");

        List<String> conteudo = new ArrayList<String>();

        try{
            conteudo = Files.readAllLines(Paths.get("database.txt"), StandardCharsets.UTF_8);
        } catch(IOException e) {
            e.printStackTrace();
        }

        int i = conteudo.indexOf(str);

        if (i == -1){
            return "Operação de exclusão finalizada. Conteudo nao existe no arquivo";
        }

        conteudo.remove(i);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("database.txt"));

            for (String s: conteudo){
                writer.write(s+"\n");
            }

            writer.close();
            TimeUnit.SECONDS.sleep(3);
        }catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(nickname + " - Exclusão realizada com sucesso");
        response = semaphoreServer.V(queryValue);

        return "Operação de exclusão finalizada";
    }

    public static void main(final String[] args) throws RemoteException {
        if (args.length != 4) {
            System.out.println("Usage: java Server <server ip> <server port> <semaphore server ip> <semaphore server port>");
            System.exit(1);
        }

        String server = "rmi://" + args[0] + ":" + args[1] + "/server";
        String semaphoreServerLink = "rmi://" + args[2] + ":" + args[3] + "/semaphore";
        semaphoreServer = null;

        try {
            System.out.println("Connecting to server at : " + semaphoreServerLink);
            semaphoreServer = (SemaphoreServerInterface) Naming.lookup(semaphoreServerLink);
        } catch (Exception e) {
            System.out.println("Client failed: ");
            e.printStackTrace();
        }

        try {
            System.setProperty("java.rmi.server.hostname", args[0]);
            LocateRegistry.createRegistry(Integer.parseInt(args[1]));
            System.out.println("java RMI registry created at: " + args[0] + ":" + args[1]);
        } catch (RemoteException e) {
            System.out.println("java RMI registry already exists.");
        }

        try {
            Naming.rebind(server, (Remote) new Server());
            System.out.println("Server is ready.");
        } catch (Exception obj) {
            System.out.println("Serverfailed: " + obj);
        }
    }
}