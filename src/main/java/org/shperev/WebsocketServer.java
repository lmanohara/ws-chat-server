package org.shperev;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;

public class WebsocketServer {

    public static final String HOST = "";
    public static final int PORT = 8081;
    public static final String WS_MAGIC_STRING = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    public ServerSocket initiateSocketServer() throws IOException {

        return new ServerSocket(PORT);
    }

    public void accept(ServerSocket serverSocket) throws IOException, NoSuchAlgorithmException {
        Socket socket = serverSocket.accept();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String inputLine;

        Map<String, String> headers = new HashMap<>();
        while ((inputLine = bufferedReader.readLine()) != null && !inputLine.equals("")) {
            if(inputLine.contains(":")) {
                String[] keyValuePair = inputLine.split(":");
                headers.put(keyValuePair[0], keyValuePair[1]);
            }
        }

        if(headers.containsKey("Upgrade") && headers.get("Upgrade").strip().equals("websocket")){
            String key = headers.get("Sec-WebSocket-Key");
            String keyPlusMagicString = key.strip() + WS_MAGIC_STRING;
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");

            byte[] digest = messageDigest.digest(keyPlusMagicString.getBytes("UTF-8"));

            String base64EncodedValue = Base64.getEncoder().encodeToString(digest);

            String response1 = "HTTP/1.1 101 Switching Protocols\r\n" +
                    "Upgrade: websocket\r\n" +
                    "Connection: Upgrade\r\n" +
                    "Sec-WebSocket-Accept: " + base64EncodedValue +
                    "\r\n\r\n";
            System.out.println(response1);

////
////            objectOutputStream.writeObject(responseHeader);
//
//            String responseHeader = "HTTP/1.1 101 Switching Protocols\r\n"
//                    + "Connection: Upgrade\r\n"
//                    + "Upgrade: websocket\r\n"
//                    + "Sec-WebSocket-Accept: "
//                    + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1")
//                    .digest((key.strip() + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
//                    + "\r\n\r\n";
//            System.out.println(responseHeader);
            byte[] response = response1.getBytes("UTF-8");

            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(response, 0, response.length);

        }

        // read from socket
        // get headers
        // validate headers
        // response handshake
    }

    private void createAcceptHeader() {


    }
}
