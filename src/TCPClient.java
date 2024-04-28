import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.Scanner;

public class TCPClient {
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private final Date date = new Date();
    private final Scanner scanner = new Scanner(System.in);
    private DataInputStream reader;
    private DataOutputStream writer;
    private Socket socket;
    private static final String QUIT = "QUIT";

    public TCPClient() throws IOException { connectToTheServer(); }

    private  void connectToTheServer() throws IOException {
        socket = new Socket("localhost", 9090);
//        socket.setSoTimeout(1000);

        reader = new DataInputStream(socket.getInputStream());
        writer = new DataOutputStream(socket.getOutputStream());
    }
    public void start() throws IOException {this.interfaceOfPage();}

    public void sendAndReadServer(String command) {
        try {
            writer.writeUTF(command);
            String response = reader.readUTF();
            System.out.println("Server response: \n" + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void interfaceOfPage() {
        for (; ;) {
            System.out.print(this.generateMainText());

            String command = scanner.nextLine();
            this.sendAndReadServer(command);

            if (command.equalsIgnoreCase(QUIT)) {
                this.cleanUp();
                break;
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

    public void cleanUp() {
        try {
            socket.close();
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
