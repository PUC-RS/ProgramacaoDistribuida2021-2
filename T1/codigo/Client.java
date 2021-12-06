import java.util.Scanner;
import java.rmi.Naming;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Client {
    public static void runRandomly(String s, ServerInterface serverInterface, String nickname) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Porcentagem de buscas:");
        int qtdBuscas = sc.nextInt();
        System.out.println("Porcentagem de inserções :");
        int qtdInsercoes = sc.nextInt();
        
        while(true) {
            int rand = new Random().nextInt(100);

            if (rand < qtdBuscas) { //60
                //Buscar
                try {
                    System.out.println(serverInterface.busca(s, nickname));
                    TimeUnit.SECONDS.sleep(2);
                }
                catch (Exception ex2) {
                    System.out.println("server.busca() falhou...");
                    ex2.printStackTrace();
                }

                System.out.println("Buscar");

            } else if (rand < (qtdBuscas + qtdInsercoes)) {   
                //Inserir
                try {
                    System.out.println(serverInterface.insere(s,nickname));
                    TimeUnit.SECONDS.sleep(2);
                }
                catch (Exception ex2) {
                    System.out.println("server.insere() falhou...");
                    ex2.printStackTrace();
                }
                System.out.println("Inserir");
            } else {
                System.out.println("Excluir");
                //Excluir
                try {
                    System.out.println(serverInterface.deleta(s,nickname));
                    TimeUnit.SECONDS.sleep(2);
                }
                catch (Exception ex2) {
                    System.out.println("server.deleta() falhou...");
                    ex2.printStackTrace();
                }
                
            }
        }
    }
    public static void main(final String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: java Client <server ip> <server port> <client ip> <\"nickname\">");
            System.exit(1);
        }

        final String server = "rmi://" + args[0] + ":" + args[1] + "/server";
        ServerInterface serverInterface = null;

        try {
            System.out.println("Connecting to server at : " + server);
            serverInterface = (ServerInterface)Naming.lookup(server);
        }
        catch (Exception ex) {
            System.out.println("Client failed: ");
            ex.printStackTrace();
        }
        
        if (serverInterface != null) {
            final Scanner scanner = new Scanner(System.in);
            int i;
            do {
                System.out.println("1 - Buscar");
                System.out.println("2 - Inserir");
                System.out.println("3 - Excluir");
                System.out.println("4 - Aleatório");
                System.out.println("0 - Sair");
                final String s = "registro1";
                i = scanner.nextInt();
                switch (i) {
                    case 1: {
                        try {
                            System.out.println(serverInterface.busca(s, args[3]));
                        }
                        catch (Exception ex2) {
                            System.out.println("server.busca() falhou...");
                            ex2.printStackTrace();
                        }
                        continue;
                    }
                    case 2: {
                        try {
                            System.out.println(serverInterface.insere(s,args[3]));
                        }
                        catch (Exception ex2) {
                            System.out.println("server.insere() falhou...");
                            ex2.printStackTrace();
                        }
                        continue;
                    }
                    case 3: {
                        try {
                            System.out.println(serverInterface.deleta(s,args[3]));
                        }
                        catch (Exception ex2) {
                            System.out.println("server.deleta() falhou...");
                            ex2.printStackTrace();
                        }
                        continue;
                    }
                    case 4: {
                        runRandomly(s, serverInterface, args[3]);
                        i = 0;
                    }
                    case 0: {
                        System.exit(0);
                        continue;
                    }
                    default: {
                        System.out.println("Op\u00e7\u00e3o inv\u00e1lida!");
                        continue;
                    }
                }
            } while (i != 0);
        }
    }
}
