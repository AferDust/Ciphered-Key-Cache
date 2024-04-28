import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        TCPClient client = new TCPClient();
//        UDPClient client = new UDPClient();
        client.start();
    }
}
