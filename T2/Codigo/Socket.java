import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Socket extends Thread {
  private final String ip;
  private final InetAddress grupo;
  private final MulticastSocket multicastSocket;

  private List<Recurso> listaRecursos = new ArrayList<>();
  private List<String> listRequisicao = new ArrayList<>();
  private InetAddress ipRemoto;
  private long lastTime;

  public Socket(String grupo, InetAddress ip) throws IOException {
    this.grupo = InetAddress.getByName(grupo);
    multicastSocket = new MulticastSocket(5000);
    this.multicastSocket.joinGroup(this.grupo);
    this.ip = ip.toString();
    this.lastTime = 0;
  }

  public List<String> getRecursos() {
    listRequisicao.clear();
    byte[] saida;
    Random rand = new Random();
    long timeSaida = System.currentTimeMillis() + rand.nextInt(1000000);
    saida = (timeSaida + "&" + ip + "&" + Tags.SUPERNODO_REQUEST_RECURSOS.toString()).getBytes();
    DatagramPacket pacote = new DatagramPacket(saida, saida.length, grupo, 5000);

    try {
      multicastSocket.send(pacote);
      Thread.sleep(2000);
    } catch (Exception e) {
      e.printStackTrace();
    }
    for (Recurso r : listaRecursos) {
      listRequisicao.add(r.getNomeRecurso());
    }
    return listRequisicao;
  }

  public InetAddress getResource(String resource) {
    ipRemoto = null;

    for (Recurso r : listaRecursos) {
      if (r.getNomeRecurso().equals(resource)) {
        ipRemoto = r.getIp();
      }
    }
    if (ipRemoto == null) {
      byte[] saida;
      Random rand = new Random();
      long timeSaida = System.currentTimeMillis() + rand.nextInt(1000000);
      saida = (timeSaida + "&" + ip + "&" + Tags.SUPERNODO_REQUEST_RECURSO.toString() + "&" + resource).getBytes();
      DatagramPacket pacote = new DatagramPacket(saida, saida.length, grupo, 5000);
      try {
        multicastSocket.send(pacote);
        Thread.sleep(2000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return ipRemoto;
  }

  public void UpdateResources(Recurso r) {
    this.listaRecursos.add(r);
  }

  @Override
  public void run() {
    Random rand = new Random();
    long timeSaida;
    boolean flSaida = false;
    while (true) {
      try {
        byte[] entrada = new byte[1024];
        DatagramPacket pacote = new DatagramPacket(entrada, entrada.length);
        multicastSocket.setSoTimeout(100);
        multicastSocket.receive(pacote);
        String recebido = new String(pacote.getData(), 0, pacote.getLength());

        String[] vars = recebido.split("&");

        if (vars[1].equals(ip) || vars[0].equals(Long.toString(lastTime)))
          continue;
        byte[] saida = new byte[1024];
        lastTime = Long.parseLong(vars[0]);
        timeSaida = System.currentTimeMillis() + rand.nextInt(1000000);

        if (vars[2].equals(Tags.SUPERNODO_REQUEST_RECURSOS.toString())) {
          String res = "&1";
          for (Recurso r : listaRecursos) {
            res = res.concat("&").concat(r.getNomeRecurso());
          }
          saida = (timeSaida + "&" + ip + "&" + vars[1] + res).getBytes();
          flSaida = true;
        } else if (vars[2].equals(Tags.SUPERNODO_REQUEST_RECURSO.toString())) {
          String res = "&2&";
          for (Recurso r : listaRecursos) {
            if (r.getNomeRecurso().equals(vars[3])) {
              res += r.getIp().toString();
            }
          }
          if (!res.equals("&2&")) {
            saida = (timeSaida + "&" + ip + "&" + vars[1] + res).getBytes();
            flSaida = true;
          }
        } else if (vars[2].equals(ip)) {
          if (vars[3].equals("1")) {
            listRequisicao.addAll(Arrays.asList(vars).subList(4, vars.length));
          } else if (vars[3].equals("2")) {
            ipRemoto = InetAddress.getByName(vars[4].substring(1));
          }
        }
        if (flSaida) {
          DatagramPacket pacote2 = new DatagramPacket(saida, saida.length, grupo, 5000);
          multicastSocket.send(pacote2);
          flSaida = false;
        }
      } catch (Exception ignored) {
      }
    }
  }
}
