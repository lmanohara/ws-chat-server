/* (C)2024 */
package org.shperev;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Main {
  public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
    WebsocketServer websocketServer = new WebsocketServer();
    ServerSocket serverSocket = websocketServer.initiateSocketServer();
    ConcurrentHashMap<UUID, Consumer<String>> clients = new ConcurrentHashMap<>();

    while (true) {
      Socket socket = websocketServer.accept(serverSocket);
      UUID clientId = UUID.randomUUID();
      clients.put(
          clientId,
          message -> {
            try {
              new ResponseWriter(socket).write(message);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });

      WebsocketReadHandler websocketHandler = new WebsocketReadHandler(clients, socket, clientId);
      websocketHandler.readMessageAsync();
    }
  }
}
