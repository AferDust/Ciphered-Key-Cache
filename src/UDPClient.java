import java.io.*;
import java.net.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UDPClient {
    private String C_UUID;
    private final int SERVER_PORT = 9091;
    private static final String QUIT = "QUIT";

    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private final Date date = new Date();
    private final Scanner scanner = new Scanner(System.in);

    private DatagramSocket socket;
    private InetAddress address;

    private static byte[] receiveData = new byte[1024];
    private static byte[] sendData = new byte[1024];


    public UDPClient() throws SocketException, UnknownHostException {
        C_UUID = UUID.randomUUID().toString();
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
    }

    public void start() { interfaceOfPage(); }

    private void sendAndReadServer(String command) {
        try {
            sendData = command.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, SERVER_PORT);
            socket.send(sendPacket);

           receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

            System.out.println("Server response: \n" + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void interfaceOfPage() {
        for (;;) {
            System.out.print(this.generateMainText());

            String command = scanner.nextLine() + " " + C_UUID;
            this.sendAndReadServer(command);

            if (command.equalsIgnoreCase(QUIT)) {
                this.cleanUp();
                System.exit(0);
            }
        }
    }

    private String generateMainText(){
        return  "\n==============================================================" +
                "\n                  Date: "  + dateFormat.format(date) +
                "\nPlease Input Command in either of the following forms:" +
                "\nGET ‹key>" +
                "\nPUT ‹key> ‹val>" +
                "\nDELETE ‹key>" +
                "\nKEYS" +
                "\nQUIT" +
                "\n\nEnter Command: ";
    }

    private void cleanUp() {
        socket.close();
        scanner.close();
        System.out.println("Client terminated.");
    }

    public static void main(String[] args) throws SocketException, UnknownHostException {
        UDPClient client = new UDPClient();
        client.start();
    }
}
