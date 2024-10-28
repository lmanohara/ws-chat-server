/* (C)2024 */
package org.shperev;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

public class Main {
  public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
    WebsocketServer websocketServer = new WebsocketServer();
    WebsocketConnection websocketConnection = new WebsocketConnection();
    ServerSocket serverSocket = websocketServer.initiateSocketServer();

    while (true) {
      Socket socket = websocketServer.accept(serverSocket);
      Runnable runnable =
          () -> {
            try {
              while (true) {
                websocketConnection.readMessage(socket);
                websocketConnection.sendResponse(socket, "Hello from server!");
              }
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          };

      Thread.ofVirtual().start(runnable);
    }
  }
}
