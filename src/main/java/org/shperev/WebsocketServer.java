/* (C)2024 */
package org.shperev;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebsocketServer {

  public static final String HOST = "";
  public static final int PORT = 8081;
  public static final String WS_MAGIC_STRING = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

  Pattern GET_REQUEST_PATTERN = Pattern.compile("^GET");
  Pattern WEB_SOCKET_KEY_PATTERN = Pattern.compile("Sec-WebSocket-Key: (.*)");

  public ServerSocket initiateSocketServer() throws IOException {
    return new ServerSocket(PORT);
  }

  public void accept(ServerSocket serverSocket)
      throws IOException, NoSuchAlgorithmException, ClassNotFoundException {
    Socket socket = serverSocket.accept();
    BufferedReader bufferedReader =
        new BufferedReader(new InputStreamReader(socket.getInputStream()));

    StringBuilder stringBuilder = new StringBuilder();
    String inputLine;
    while ((inputLine = bufferedReader.readLine()) != null && !inputLine.equals("")) {
      stringBuilder.append(inputLine).append("\n");
    }

    String handShakeRequest = stringBuilder.toString();

    if (checkWebSocketHeaders(handShakeRequest)) {
      Matcher matcher = WEB_SOCKET_KEY_PATTERN.matcher(handShakeRequest);
      matcher.find();

      String keyPlusMagicString = matcher.group(1).strip() + WS_MAGIC_STRING;
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");

      byte[] digest = messageDigest.digest(keyPlusMagicString.getBytes("UTF-8"));

      String base64EncodedValue = Base64.getEncoder().encodeToString(digest);

      String response1 =
          "HTTP/1.1 101 Switching Protocols\r\n"
              + "Upgrade: websocket\r\n"
              + "Connection: Upgrade\r\n"
              + "Sec-WebSocket-Accept: "
              + base64EncodedValue
              + "\r\n\r\n";
      System.out.println(response1);

      byte[] response = response1.getBytes("UTF-8");

      OutputStream outputStream = socket.getOutputStream();

      outputStream.write(response, 0, response.length);

      bufferedReader.close();
      outputStream.close();
    }

    // read from socket
    // get headers
    // validate headers
    // response handshake
  }

  private void createAcceptHeader() {}

  private boolean checkWebSocketHeaders(String handShakeRequest) {

    return GET_REQUEST_PATTERN.matcher(handShakeRequest).find()
        && WEB_SOCKET_KEY_PATTERN.matcher(handShakeRequest).find();
  }
}
