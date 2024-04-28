import exceptions.InvalidArrayLengthException;
import exceptions.InvalidStringLengthException;

import java.io.*;
import java.net.*;
import java.util.*;

public class TCPServer {
    private static final String QUIT = "QUIT";
    private static final String KEYS = "KEYS";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";
    private static final String GET = "GET";

    private static int clientCounter = 0;

    public static void main(String[] args) {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(9090)) {

                for (; ; ) {
                    Socket socket = serverSocket.accept();
                    System.out.println("Client " + (++clientCounter) + " accept.");

                    HashMap<String, String> keyValStore = new HashMap<>();
                    new Thread(() -> {
                        try (DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
                             DataInputStream reader = new DataInputStream(socket.getInputStream())
                        ) {
                            for (; ; ) {
                                System.out.println("Step in try");
                                handleClientRequest(writer, reader, keyValStore);
                            }
                        } catch (IOException b) {
                            b.printStackTrace();
                        }
                    }).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void handleClientRequest(DataOutputStream writer, DataInputStream reader, HashMap<String, String> keyValStore) throws IOException {
        String response = "";

        try {
            String command = reader.readUTF();
            String[] parts = command.split("\\s+");

            response = switch (parts[0].toUpperCase()) {
                case KEYS -> handleKeysRequest(keyValStore);
                case PUT -> handlePutRequest(parts, keyValStore);
                case DELETE -> handleDeleteRequest(parts, keyValStore);
                case GET -> handleGetRequest(parts, keyValStore);
                case QUIT -> "Goodbye!";
                default -> "Invalid command";
            };

            System.out.println("Return response: " + response);
        } catch (Exception exception) {
            System.out.println("Catch exception");
            response = exception.getMessage();
        } finally {
            writer.writeUTF(response);
        }
    }

    private static String handlePutRequest(String[] parts, HashMap<String, String> keyValStore) throws InvalidArrayLengthException, InvalidStringLengthException {
        if (parts.length != 3)
            throw new InvalidArrayLengthException("Invalid command format");
        if(parts[1].length() > 10 || parts[2].length() > 10)
            throw new InvalidStringLengthException("Key or Value too long (max: 10 character)");

        keyValStore.put(parts[1], parts[2]);
        return "Successfully added key-value pair";
    }

    private static String handleKeysRequest(HashMap<String, String> keyValStore) {
        return keyValStore.keySet().toString().isEmpty()
                ? "No keys in database."
                : "Keys: " + keyValStore.keySet();
    }

    private static String handleDeleteRequest(String[] parts, HashMap<String, String> keyValStore) throws InvalidArrayLengthException {
        if (parts.length != 2)
            throw new InvalidArrayLengthException("Invalid command format");
        if (!keyValStore.containsKey(parts[1]))
            throw new IllegalArgumentException("Key " + parts[1] + " not found in the map");

        keyValStore.remove(parts[1]);
        return "Successfully deleted key";
    }

    private static String handleGetRequest(String[] parts, HashMap<String, String> keyValStore) throws InvalidArrayLengthException {
        if (parts.length != 2)
            throw new InvalidArrayLengthException("Invalid command format");
        if (!keyValStore.containsKey(parts[1]))
            throw new IllegalArgumentException("Key " + parts[1] + " not found in the map");

        return keyValStore.get(parts[1]);
    }
}