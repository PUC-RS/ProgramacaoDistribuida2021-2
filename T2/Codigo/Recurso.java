import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Recurso {
  private String hash;
  private InetAddress ip;
  private String nomeRecurso;

  public Recurso(String hash, InetAddress ip, String nomeRecurso) {
    try {
      MessageDigest algorithm = MessageDigest.getInstance("SHA-256");
      byte[] b = algorithm.digest(hash.getBytes());
      this.hash = new String(b, 0, b.length);
      this.ip = ip;
      this.nomeRecurso = nomeRecurso;
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
  }

  public String getHash() {
    return hash;
  }

  public InetAddress getIp() {
    return ip;
  }

  public String getNomeRecurso() {
    return nomeRecurso;
  }
}