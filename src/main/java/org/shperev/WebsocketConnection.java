/* (C)2024 */
package org.shperev;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class WebsocketConnection {

  public String readMessage(Socket socket) throws IOException {
    InputStream inputStream = socket.getInputStream();

    int firstFrame = Byte.toUnsignedInt((byte) inputStream.read());
    int finBit = firstFrame & 128; // extract fin bit applying & masking

    // check fin bit is zero for no continuation of payload
    boolean isFinBitOne = finBit == 128;
    int opCode =
        (byte) firstFrame & (byte) 15; // Apply bitwise & to represent opcode between 0 - 15
    boolean isOpCodeText = opCode == 1;

    if (isFinBitOne & isOpCodeText) {
      int unsignedSecondFrame = Byte.toUnsignedInt((byte) inputStream.read());
      int maskBit = unsignedSecondFrame & 128;
      boolean isResponseMasked = maskBit == 128;
      if (isResponseMasked) {
        // read next 4 bytes for mask
        byte[] maskBytes = inputStream.readNBytes(4);

        int payloadLength = unsignedSecondFrame - 128;
        byte[] payload;
        switch (payloadLength) {
          case 126 -> payload = inputStream.readNBytes(2); // read next 2 bytes
          case 127 -> payload = inputStream.readNBytes(8); // read next 8 bytes
          default ->
              payload = inputStream.readNBytes(payloadLength); // read next bytes until the length
        }

        byte[] unmaskedBytes = new byte[payload.length];
        for (int i = 0; i < payload.length; i++) {
          int maskingKeyIndex = i % 4;
          unmaskedBytes[i] =
              (byte)
                  (Byte.toUnsignedInt(maskBytes[maskingKeyIndex]) ^ Byte.toUnsignedInt(payload[i]));
        }

        String message = new String(unmaskedBytes);
        System.out.println(message);

        return message;
      }
    }
    System.out.println(firstFrame);
    return "";
  }
}
