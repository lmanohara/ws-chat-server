/* (C)2024 */
package org.shperev;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class WebsocketConnection {

  public void readMessage(Socket socket) throws IOException {
    InputStream inputStream = socket.getInputStream();
    byte[] responseBytes = new byte[1024];
    inputStream.read(responseBytes);
    int unsignedFinByte = Byte.toUnsignedInt(responseBytes[0]);
    String firstFrame = Integer.toBinaryString(unsignedFinByte);

    // check fin bit is zero for no continuation of payload
    boolean isFinBitOne = firstFrame.charAt(0) == '1';
    int opCode =
        (byte) unsignedFinByte & (byte) 15; // Apply bitwise & to represent opcode between 0 - 15
    boolean isOpCodeText = opCode == 1;

    if (isFinBitOne & isOpCodeText) {
      int unsignedSecondFrame = Byte.toUnsignedInt(responseBytes[1]);
      String secondFrameBits = Integer.toBinaryString(unsignedSecondFrame);
      boolean isResponseMasked = secondFrameBits.charAt(0) == '1';
      if (isResponseMasked) {

        int payloadLength = unsignedSecondFrame - 128;
        byte[] payload;
        if (payloadLength == 126) {
          payload = Arrays.copyOfRange(responseBytes, 6, 9);
        } else if (payloadLength == 127) {
          payload = Arrays.copyOfRange(responseBytes, 6, 15);
        } else {
          payload = Arrays.copyOfRange(responseBytes, 6, 6 + payloadLength);
        }

        byte[] maskBytes = Arrays.copyOfRange(responseBytes, 2, 6);
        byte[] unmaskedBytes = new byte[payload.length];
        for (int i = 0; i < payload.length; i++) {
          int maskingKeyIndex = i % 4;
          unmaskedBytes[i] =
              (byte)
                  (Byte.toUnsignedInt(maskBytes[maskingKeyIndex]) ^ Byte.toUnsignedInt(payload[i]));
        }

        System.out.println(new String(unmaskedBytes));
      }
    }
    System.out.println(firstFrame);
  }

  public void sendResponse(Socket socket, String message) throws IOException {
    OutputStream outputStream = socket.getOutputStream();

    byte[] messageInBytes = message.getBytes(StandardCharsets.UTF_8);
    outputStream.write(129);
    outputStream.write((byte) messageInBytes.length);
    outputStream.write(messageInBytes);

    outputStream.flush();
  }
}
