/* (C)2024 */
package org.shperev;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ResponseWriter {

  Socket socket;

  public ResponseWriter(Socket socket) {
    this.socket = socket;
  }

  public void write(String message) throws IOException {
    OutputStream outputStream = socket.getOutputStream();

    byte[] messageInBytes = message.getBytes(StandardCharsets.UTF_8);
    outputStream.write(129);

    int payloadLength = messageInBytes.length;
    if (payloadLength >= 126 && payloadLength < 65536) {
      payloadLength = 126;
    } else if (payloadLength >= 65536) {
      payloadLength = 127;
    }
    outputStream.write((byte) payloadLength);
    outputStream.write(messageInBytes);

    outputStream.flush();
  }
}
