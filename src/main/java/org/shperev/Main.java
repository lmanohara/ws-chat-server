/* (C)2024 */
package org.shperev;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Main {
  public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
    WebsocketServer websocketServer = new WebsocketServer();
    websocketServer.start();
  }
}
