/* (C)2024 */
package org.shperev;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.NoSuchAlgorithmException;

public class Main {
  public static void main(String[] args)
      throws IOException, NoSuchAlgorithmException, ClassNotFoundException {
    WebsocketServer websocketServer = new WebsocketServer();

    ServerSocket serverSocket = websocketServer.initiateSocketServer();
    while (true) {
      websocketServer.accept(serverSocket);
    }
  }
}
