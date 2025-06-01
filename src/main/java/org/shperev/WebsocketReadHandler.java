/* (C)2024 */
package org.shperev;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class WebsocketReadHandler {

  Map<UUID, Consumer<String>> clients;
  Socket receiver;
  UUID clientId;

  public WebsocketReadHandler(Map<UUID, Consumer<String>> clients, Socket receiver, UUID clientId) {
    this.clients = clients;
    this.receiver = receiver;
    this.clientId = clientId;
  }

  public void readMessage() {
    Runnable runnable =
        () -> {
          try {
            while (true) {
              WebsocketConnection websocketConnection = new WebsocketConnection();
              String message = websocketConnection.readMessage(receiver);
              clients.entrySet().stream()
                  .filter(client -> !client.getKey().equals(clientId))
                  .forEach(client -> client.getValue().accept(message));
            }
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        };

    Thread.ofVirtual().start(runnable);
  }

  public void readMessageAsync() {
    WebsocketConnection websocketConnection = new WebsocketConnection();
    CompletableFuture.supplyAsync(
            () -> {
              try {
                return websocketConnection.readMessage(receiver);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .thenAccept(
            msg ->
                clients.entrySet().stream()
                    //                    .filter(client -> !client.getKey().equals(clientId))
                    .forEach(client -> client.getValue().accept(msg)));
  }
}
