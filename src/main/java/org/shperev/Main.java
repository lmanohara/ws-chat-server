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

    Socket socket;
    socket = websocketServer.accept(serverSocket);

    //      do{
    //        websocketConnection.readMessage(socket);
    //      } while (!socket.isClosed());
    //      while (true){

    while (true) {

      websocketConnection.readMessage(socket);
    }
    //      }
  }
}
