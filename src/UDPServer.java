import exceptions.EmptyListException;
import exceptions.InvalidArrayLengthException;
import exceptions.InvalidStringLengthException;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;



public class UDPServer {
    private static final int SERVER_PORT = 9091;

    private static final String QUIT = "QUIT";
    private static final String KEYS = "KEYS";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";
    private static final String GET = "GET";

    private static HashMap<String, HashMap<String, String>> clientKeyValueDatabase  = new HashMap<>();
    private static byte[] receiveData = new byte[1024];
    private static byte[] sendData = new byte[1024];

    private static void startServer() {
        try (DatagramSocket serverSocket = new DatagramSocket(SERVER_PORT)) {
            for (; ; ) {
                new Thread(() -> {
                    DatagramPacket receivePacket;
                    DatagramPacket sendPacket;
                    try {
                        for (; ; ) {
                            receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            serverSocket.receive(receivePacket);

                            String command = new String(receivePacket.getData(), 0, receivePacket.getLength());
                            System.out.println(command);
                            int port = receivePacket.getPort();

                            String response = handleCommand(command);
                            System.out.println(response);

                            sendData = response.getBytes();
                            sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), port);
                            serverSocket.send(sendPacket);
                        }
                    } catch (IOException exception) {
                        System.err.println("Error: " + exception.getMessage());
                        exception.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static String handleCommand(String command) {
        String response = "";

        try {
            List<String> parts = Arrays.asList(command.split("\\s+"));
            String uuid = parts.get(parts.size() - 1);

            if (!clientKeyValueDatabase.containsKey(uuid))
                clientKeyValueDatabase.put(uuid, new HashMap<>());

            HashMap<String, String> keyValStore = clientKeyValueDatabase.get(uuid);

            if (parts.size() == 1)
                throw new EmptyListException("Empty message.");

            response = switch (parts.get(0).toUpperCase()) {
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
        }

        return response;
    }

    private static String handlePutRequest(List<String> parts, HashMap<String, String> keyValStore) throws InvalidArrayLengthException, InvalidStringLengthException {
        if (parts.size() != 4)
            throw new InvalidArrayLengthException("Invalid command format");
        if(parts.get(1).length() > 10 || parts.get(2).length() > 10)
            throw new InvalidStringLengthException("Key or Value too long (max: 10 character)");

        keyValStore.put(parts.get(1), parts.get(2));
        return "Successfully added key-value pair";
    }

    private static String handleKeysRequest(HashMap<String, String> keyValStore) {
        return keyValStore.keySet().isEmpty()
                ? "No keys in database."
                : "Keys: " + keyValStore.keySet();
    }

    private static String handleDeleteRequest(List<String> parts, HashMap<String, String> keyValStore) throws InvalidArrayLengthException {
        if (parts.size() != 3)
            throw new InvalidArrayLengthException("Invalid command format");
        if (!keyValStore.containsKey(parts.get(1)))
            throw new IllegalArgumentException("Key {" + parts.get(1) + "} not found in the map");

        keyValStore.remove(parts.get(1));
        return "Successfully deleted key";
    }

    private static String handleGetRequest(List<String> parts, HashMap<String, String> keyValStore) throws InvalidArrayLengthException {
        if (parts.size() != 3)
            throw new InvalidArrayLengthException("Invalid command format");
        if (!keyValStore.containsKey(parts.get(1)))
            throw new IllegalArgumentException("Key {" +parts.get(1) + "} not found in the map");

        return keyValStore.get(parts.get(1));
    }

    public static void main(String[] args) { startServer(); }
}