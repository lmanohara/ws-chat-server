/* (C)2024 */
package org.shperev;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketAsyncMessageHandler {

  //  Map<UUID, Consumer<String>> clients;
  //  Socket receiver;
  //  UUID clientId;

  private final RequestReader requestReader;
  private final ResponseWriter responseWriter;

  Logger log = LoggerFactory.getLogger(WebsocketAsyncMessageHandler.class);

  ExecutorService pool = Executors.newCachedThreadPool();

  public WebsocketAsyncMessageHandler() {
    this.requestReader = new RequestReader();
    this.responseWriter = new ResponseWriter();
  }

  //  public void readMessage() {
  //    Runnable runnable =
  //        () -> {
  //          try {
  //            while (true) {
  //              RequestReader websocketConnection = new RequestReader();
  //              String message = websocketConnection.readMessage(receiver);
  //              clients.entrySet().stream()
  //                  .filter(client -> !client.getKey().equals(clientId))
  //                  .forEach(client -> client.getValue().accept(message));
  //            }
  //          } catch (IOException e) {
  //            throw new RuntimeException(e);
  //          }
  //        };
  //
  //    Thread.ofVirtual().start(runnable);
  //  }

  public CompletableFuture<Void> readMessageAsync(
      ConcurrentHashMap<Session, BlockingQueue<String>> clients, Session client) {
    return CompletableFuture.runAsync(
        () -> {
          try {
            log.info("Read message async");
            Socket receiver = client.getSocket();
            log.info(
                "Websocket client id: {} read status: {}",
                client.getClientId(),
                receiver.isClosed());
            while (!receiver.isClosed()) {
              String message = requestReader.readMessage(receiver);
              if (message != null) {
                clients.forEach((session, queue) -> queue.offer(message));
              }
            }
          } catch (IOException ex) {
            throw new RuntimeException("Error reading Websocket message", ex);
          }
        },
        pool);
  }

  public CompletableFuture<Void> writeMessageAsync(Session client, BlockingQueue<String> queue) {
    return CompletableFuture.runAsync(
        () -> {
          log.info("Write message async");
          Socket socket = client.getSocket();
          log.info(
              "Websocket client id: {} write status: {}", client.getClientId(), socket.isClosed());
          while (!socket.isClosed()) {
            try {
              String message = queue.take();
              responseWriter.write(socket, message);
            } catch (InterruptedException | IOException e) {
              throw new RuntimeException(e);
            } finally {
              // remove client and disconnect
            }
          }
        },
        pool);
  }
}
