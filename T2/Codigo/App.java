import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class App {

  public static void main(String[] args) throws IOException {
    if (args.length != 4 && args[1].equals("Supernodo")){
            System.out.println("Uso para Supernodo: java App Supernodo <IP Maquina> <IP Grupo> <Porta Supernodo>");
      System.exit(1);

    }

    if (args.length != 5 && args[1].equals("Nodo")){
      System.out.println("Uso para Nodo: java App Nodo <IP Maquina> <IP Supernodo> <Porta Supernodo> <Porta Nodo>");
      System.exit(1);
    }

    Socket multicast = null;
    List<Recurso> listaRecursos = new ArrayList<>();
    Map<InetAddress, Integer> nodosAssociados = new HashMap<>();

    InetAddress ip = InetAddress.getByName(args[1]);
    // SUPERNODO
    if (args[0].equals("Supernodo")) {
      multicast = new Socket(args[2], ip);
      multicast.start();

      DatagramSocket socket = new DatagramSocket(Integer.parseInt(args[3]));
      while (true) {
        try {
          byte[] entrada = new byte[1024];
          DatagramPacket pacote = new DatagramPacket(entrada, entrada.length);
          socket.setSoTimeout(100);
          socket.receive(pacote);
          String recebido = new String(pacote.getData(), 0, pacote.getLength());

          String[] vars = recebido.split("&");

          if (vars[0].equals(Tags.REGISTRA_NOVO_NODO.toString())) {
            System.out.println("Chegou registro: Nodo " + pacote.getAddress());

            // Thread para receber KA
            new Thread() {
              @Override
              public void run() {
                long horaRecebido = System.currentTimeMillis();
                while (true) {
                  DatagramPacket pacoteIsAlive = null;
                  try {
                    byte[] isAlive = new byte[1024];
                    pacoteIsAlive = new DatagramPacket(isAlive, isAlive.length);
                    socket.receive(pacoteIsAlive);
                    String recebido = new String(pacoteIsAlive.getData(), 0, pacoteIsAlive.getLength());

                    horaRecebido = System.currentTimeMillis();

                    if (recebido.equals(Tags.ALIVE.toString())) {
                      horaRecebido = System.currentTimeMillis();
                      System.out.println("Nodo: " + pacoteIsAlive.getAddress() + " está vivo");
                    }

                    try {
                    Thread.sleep(5000);
                  } catch (InterruptedException e){
                    e.printStackTrace();
                  }
                  } catch (IOException e) {
                    if (System.currentTimeMillis() - horaRecebido > 30000) {
                      System.out.println("Nodo " + pacoteIsAlive.getAddress() + " desconectado");
                      break;
                    }
                  }
                  
                }
              }
            }.start();

            nodosAssociados.put(pacote.getAddress(), pacote.getPort());
            String hash;
            String nome;
            for (int i = 1; i < vars.length; i = i + 2) {
              hash = vars[i];
              nome = vars[i + 1];
              listaRecursos.add(new Recurso(hash, pacote.getAddress(), nome));
              multicast.UpdateResources(new Recurso(hash, pacote.getAddress(), nome));
            }
          } else if (vars[0].equals(Tags.NODO_REQUEST_RECURSOS.toString())) {
            InetAddress solicitante = pacote.getAddress();
            System.out.println("Chegou Resources request");
            List<String> retornoSolicitacao = multicast.getRecursos();
            String envioResp = Tags.NODO_RESPONSE_RECURSOS.toString();
            for (String s : retornoSolicitacao) {
              envioResp += "&" + s;
            }
            byte[] envio = envioResp.getBytes();
            pacote = new DatagramPacket(envio, envio.length, solicitante, nodosAssociados.get(solicitante));
            socket.send(pacote);

          } else if (vars[0].equals(Tags.NODO_REQUEST_RECURSO.toString())) {
            System.out.println("Chegou Resource request");
            InetAddress IPResource = multicast.getResource(vars[1]);
            InetAddress solicitante = pacote.getAddress();
            String envioResp = Tags.NODO_RESPONSE_RECURSO + "&" + IPResource.toString();
            byte[] envio = envioResp.getBytes();
            pacote = new DatagramPacket(envio, envio.length, solicitante, nodosAssociados.get(solicitante));
            socket.send(pacote);
          }
        } catch (Exception ignored) {
        }
      }
    }
    // NODO P2P
    else if (args[0].equals("Nodo")) {
      File file = new File("./Recursos");
      File[] arquivos = file.listFiles();
      if (arquivos != null) {
        for (File f : arquivos) {
          String conteudo = leitor(f);
          listaRecursos.add(new Recurso(conteudo, ip, f.getName()));
        }
      }
      DatagramPacket pacote;

      byte[] registro = new byte[1024];
      DatagramSocket socket = new DatagramSocket(Integer.parseInt(args[4]));
      String toSend = "";

      for (Recurso r : listaRecursos) {
        toSend += "&" + r.getHash() + "&" + r.getNomeRecurso();
      }
      registro = (Tags.REGISTRA_NOVO_NODO + toSend).getBytes();
      InetAddress enderecoSupernodo = InetAddress.getByName(args[2]);

      try {
        pacote = new DatagramPacket(registro, registro.length, enderecoSupernodo, Integer.parseInt(args[3]));
        socket.send(pacote);

        Timer timer = new Timer();
            timer.schedule(new TimerTask(){
              @Override
              public void run(){
                try {
                byte[] keepAlive = (Tags.ALIVE.toString()).getBytes();
                DatagramPacket pacote = new DatagramPacket(keepAlive, keepAlive.length, enderecoSupernodo,
                    Integer.parseInt(args[3]));
                socket.send(pacote);
              } catch (Exception ignored) {
              }
              }
            }, 0, 5000);
      } catch (IOException e) {
        socket.close();
      }
      System.out.println("=================================");
      System.out.println("| Escolha sua requisição:       |");
      System.out.println("|1 - Solicitar Todos Resources  |");
      System.out.println("|2 - Solicitar IP Resource      |");
      System.out.println("=================================");
      while (true) {
        try {
          byte[] entrada = new byte[1024];
          DatagramPacket pacoteResposta = new DatagramPacket(entrada, entrada.length);
          socket.setSoTimeout(100);
          socket.receive(pacoteResposta);
          String recebido = new String(pacoteResposta.getData(), 0, pacoteResposta.getLength());

          String[] vars = recebido.split("&");

          if (vars[0].equals(Tags.NODO_RESPONSE_RECURSOS.toString())) {
            for (int i = 1; i < vars.length; i++) {
              System.out.println(vars[i]);
            }
          } else if (vars[0].equals(Tags.NODO_RESPONSE_RECURSO.toString())) {
            System.out.println("Local Resource: " + vars[1]);
          } else if (vars[0].equals(Tags.P2P_REQUEST.toString())) {
            String conteudo = "";
            String hashEnvio = "";
            for (Recurso r : listaRecursos) {
              if (vars[1].equals(r.getNomeRecurso())) {
                hashEnvio = r.getHash();
                file = new File("./Recursos");
                arquivos = file.listFiles();
                if (arquivos != null)
                  for (File f : arquivos)
                    if (vars[1].equals(f.getName()))
                      conteudo = leitor(f);
              }
            }
            if (!conteudo.equals("")) {
              byte[] envioRec = (Tags.P2P_RESPONSE.toString() + "&" + vars[1] + "&" + conteudo + "&" + hashEnvio)
                  .getBytes();
              DatagramPacket pacoteRec = new DatagramPacket(envioRec, envioRec.length, pacoteResposta.getAddress(),
                  pacoteResposta.getPort());
              socket.send(pacoteRec);
              System.out.println("Resource Enviado!");
            } else
              System.out.println("Resource não encontrado... :C");
          } else if (vars[0].equals(Tags.P2P_RESPONSE.toString())) {
            System.out.println("Resource Recebido!");
            listaRecursos.add(new Recurso(vars[2], ip, vars[1]));
            escritor(vars[1], vars[2]);
            System.out.println("Resource Adicionado Corretamente!");
          }
        } catch (Exception ignored) {
        }
        Scanner scanner = new Scanner(System.in);
        if (System.in.available() > 0) {
          byte[] saida;
          int mens = scanner.nextInt();
          switch (mens) {
          case 1:
            saida = (Tags.NODO_REQUEST_RECURSOS.toString()).getBytes();
            pacote = new DatagramPacket(saida, saida.length, enderecoSupernodo, Integer.parseInt(args[3]));
            socket.send(pacote);
            break;
          case 2:
            Scanner sc = new Scanner(System.in);
            System.out.println("Qual Resource?");
            String resSolicitado = sc.nextLine();
            saida = (Tags.NODO_REQUEST_RECURSO.toString() + "&" + resSolicitado).getBytes();
            pacote = new DatagramPacket(saida, saida.length, enderecoSupernodo, Integer.parseInt(args[3]));
            socket.send(pacote);
            break;
          default:
            System.out.println("=================================");
            System.out.println("| Escolha sua requisição:       |");
            System.out.println("|1 - Solicitar Todos Recursos   |");
            System.out.println("|2 - Solicitar IP Recurso       |");
            System.out.println("=================================");
            break;
          }
        }
      }
    } else
      System.out.println("Configurações Inválidas!");
  }

  public static String leitor(File arq) throws IOException {
    BufferedReader buffRead = new BufferedReader(new FileReader(arq));
    String linha = "";
    String conteudo = "";
    while (true) {
      if (linha != null) {
        conteudo += linha;
      } else
        break;
      linha = buffRead.readLine();
    }
    buffRead.close();
    return conteudo;
  }

  public static void escritor(String nomeArq, String conteudo) throws IOException {
    BufferedWriter buffWrite = new BufferedWriter(new FileWriter("./Recursos/" + nomeArq));
    buffWrite.append(conteudo + "\n");
    buffWrite.close();
  }
}
