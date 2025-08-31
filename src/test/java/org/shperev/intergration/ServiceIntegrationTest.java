/* (C)2025 */
package org.shperev.intergration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.shperev.WebsocketServer;

@ClientEndpoint
public class ServiceIntegrationTest {

  private static BlockingQueue<String> messageQueue;

  @BeforeAll
  static void startServer() throws IOException, NoSuchAlgorithmException {
    WebsocketServer websocketServer = new WebsocketServer();
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    executorService.submit(
        () -> {
          try {
            websocketServer.start();
          } catch (IOException e) {
            throw new RuntimeException(e);
          } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
          }
        });
    messageQueue = new LinkedBlockingQueue<>();
  }

  @OnOpen
  public void onOpen(Session session) {
    System.out.println("Connected to websocket server");
  }

  @OnMessage
  public void onMessage(String message) {
    messageQueue.offer(message);
  }

  @Test
  void testEchoMessage() throws DeploymentException, IOException, InterruptedException {
    WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
    Session session = webSocketContainer.connectToServer(this, URI.create("ws://localhost:8081"));

    String testMessage = "Ping";
    session.getBasicRemote().sendText(testMessage);

    String response = messageQueue.poll(5, TimeUnit.SECONDS);

    assertEquals("Ping", response);
    session.close();
  }
}
