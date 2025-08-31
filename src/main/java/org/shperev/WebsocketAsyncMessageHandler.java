/* (C)2024 */
package org.shperev;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketAsyncMessageHandler {
  private final RequestReader requestReader;
  private final ResponseWriter responseWriter;

  private final ConcurrentHashMap<Session, BlockingQueue<String>> clients;

  Logger log = LoggerFactory.getLogger(WebsocketAsyncMessageHandler.class);

  ExecutorService pool = Executors.newCachedThreadPool();

  public WebsocketAsyncMessageHandler(ConcurrentHashMap<Session, BlockingQueue<String>> clients) {
    this.clients = clients;
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

  public CompletableFuture<Void> readMessageAsync(Session client) {
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
                clients.entrySet().stream()
                    .filter(entry -> !entry.getKey().equals(client))
                    .forEach(entry -> entry.getValue().offer(message));
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
              disconnect(client);
            }
          }
        },
        pool);
  }

  private void disconnect(Session session) {
    try {
      Socket client = session.getSocket();
      if (client.isClosed() || !client.isConnected()) {
        clients.remove(session);
        client.close();
        log.info("Client disconnected: {}", session.getClientId());
      }
    } catch (IOException e) {
      throw new RuntimeException("Unable to closed client.", e);
    }
  }
}
