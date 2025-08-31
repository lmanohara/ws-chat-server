/* (C)2024 */
package org.shperev;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketServer {

  public static final String HOST = "";
  public static final int PORT = 8081;
  public static final String WS_MAGIC_STRING = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
  private static final Logger log = LoggerFactory.getLogger(WebsocketServer.class);
  Pattern GET_REQUEST_PATTERN = Pattern.compile("^GET");
  Pattern WEB_SOCKET_KEY_PATTERN = Pattern.compile("Sec-WebSocket-Key: (.*)");

  private ServerSocket initiateSocketServer() throws IOException {
    return new ServerSocket(PORT);
  }

  public Socket accept(ServerSocket serverSocket) throws IOException, NoSuchAlgorithmException {
    Socket socket = serverSocket.accept();

    BufferedReader bufferedReader =
        new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

    StringBuilder stringBuilder = new StringBuilder();
    String inputLine;
    while ((inputLine = bufferedReader.readLine()) != null && !inputLine.equals("")) {
      stringBuilder.append(inputLine).append("\n");
    }

    String handShakeRequest = stringBuilder.toString();
    log.info(handShakeRequest);

    if (checkWebSocketHeaders(handShakeRequest)) {
      Matcher matcher = WEB_SOCKET_KEY_PATTERN.matcher(handShakeRequest);
      matcher.find();

      String keyPlusMagicString = matcher.group(1).strip() + WS_MAGIC_STRING;
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");

      byte[] digest = messageDigest.digest(keyPlusMagicString.getBytes(StandardCharsets.UTF_8));

      String base64EncodedValue = Base64.getEncoder().encodeToString(digest);

      String response1 =
          "HTTP/1.1 101 Switching Protocols\r\n"
              + "Upgrade: websocket\r\n"
              + "Connection: Upgrade\r\n"
              + "Sec-WebSocket-Accept: "
              + base64EncodedValue
              + "\r\n\r\n";
      System.out.println(response1);

      byte[] response = response1.getBytes(StandardCharsets.UTF_8);

      OutputStream outputStream = socket.getOutputStream();

      outputStream.write(response, 0, response.length);

      //      bufferedReader.close();
      //      outputStream.close();
    } else {
      // Todo response with 400 not support
    }

    // read from socket
    // get headers
    // validate headers
    // response handshake

    return socket;
  }

  private void createAcceptHeader() {}

  private boolean checkWebSocketHeaders(String handShakeRequest) {

    return GET_REQUEST_PATTERN.matcher(handShakeRequest).find()
        && WEB_SOCKET_KEY_PATTERN.matcher(handShakeRequest).find();
  }

  public void start() throws IOException, NoSuchAlgorithmException {
    log.info("Websocket server started and listening to port {}", PORT);
    ServerSocket serverSocket = initiateSocketServer();
    ConcurrentHashMap<Session, BlockingQueue<String>> clients = new ConcurrentHashMap<>();
    WebsocketAsyncMessageHandler websocketAsyncMessageHandler =
        new WebsocketAsyncMessageHandler(clients);

    while (true) {
      Socket socket = accept(serverSocket);

      BlockingQueue<String> queue = new LinkedBlockingQueue<>();
      Session session = new Session(socket);
      log.info("New client join to the server with client id {}", session.getClientId());
      clients.put(session, queue);

      websocketAsyncMessageHandler.readMessageAsync(session);
      websocketAsyncMessageHandler.writeMessageAsync(session, queue);
    }
  }
}
