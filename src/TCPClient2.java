import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class TCPClient2 {
    private Socket socket;
    private DataOutputStream writer;
    private DataInputStream reader;
    private Scanner scanner;

    public TCPClient2(String address, int port) {
        try {
            socket = new Socket(address, port);
            System.out.println("Connected to the server at " + address + ":" + port);
            writer = new DataOutputStream(socket.getOutputStream());
            reader = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        scanner = new Scanner(System.in);
    }

    public void sendCommand(String command) {
        try {
            writer.writeUTF(command);
            writer.flush();  // Make sure to flush the output stream to send the data immediately.
            String response = reader.readUTF();
            System.out.println("Server response: " + response);
        } catch (IOException e) {
            System.out.println("Error during communication: " + e.getMessage());
        }
    }

    public void startClient() {
        String input;
        try {
            do {
                System.out.println("Enter command (PUT, GET, DELETE, KEYS, QUIT):");
                input = scanner.nextLine();
                sendCommand(input);

                if (input.equalsIgnoreCase("QUIT")) {
                    closeEverything();
                }
            } while (!input.equalsIgnoreCase("QUIT"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeEverything() {
        try {
            if (scanner != null) scanner.close();
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
            System.out.println("Connection closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TCPClient2 client = new TCPClient2("localhost", 9090);
        client.startClient();
    }
}
