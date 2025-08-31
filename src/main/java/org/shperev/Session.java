/* (C)2025 */
package org.shperev;

import java.net.Socket;
import java.util.UUID;

public class Session {
  private final String clientId;
  private final Socket socket;

  public Session(Socket socket) {
    this.socket = socket;
    this.clientId = UUID.randomUUID().toString();
  }

  public Socket getSocket() {
    return this.socket;
  }

  public String getClientId() {
    return this.clientId;
  }
}
