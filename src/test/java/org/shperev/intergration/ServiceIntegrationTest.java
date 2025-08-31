/* (C)2025 */
package org.shperev.intergration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

public class ServiceIntegrationTest {

  @BeforeAll
  static void startServer() {
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
    try {
      Thread.sleep(500); // wait for server to boot
    } catch (InterruptedException ignored) {
    }
  }

  @ClientEndpoint
  public static class TestClient {
    private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();

    @OnOpen
    public void onOpen(Session session) {
      System.out.println("Connected: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message) {
      messages.offer(message);
    }

    public BlockingQueue<String> getMessages() {
      return messages;
    }
  }

  @Test
  void testMessagePassingBetweenTwoClients()
      throws DeploymentException, IOException, InterruptedException {

    WebSocketContainer container = ContainerProvider.getWebSocketContainer();

    // Create client1 and connect
    TestClient client1 = new TestClient();
    Session session1 = container.connectToServer(client1, URI.create("ws://localhost:8081"));

    // Create client2 and connect
    TestClient client2 = new TestClient();
    Session session2 = container.connectToServer(client2, URI.create("ws://localhost:8081"));

    // --- Send message from client1 -> expect client2 receives ---
    String msg1 = "Hello from client1";
    session1.getBasicRemote().sendText(msg1);

    String receivedByClient2 = client2.getMessages().poll(5, TimeUnit.SECONDS);
    assertNotNull(receivedByClient2, "Client2 should receive a message");
    assertEquals(msg1, receivedByClient2);

    // --- Send message from client2 -> expect client1 receives ---
    String msg2 = "Hello back from client2";
    session2.getBasicRemote().sendText(msg2);

    String receivedByClient1 = client1.getMessages().poll(5, TimeUnit.SECONDS);
    assertNotNull(receivedByClient1, "Client1 should receive a message");
    assertEquals(msg2, receivedByClient1);

    session1.close();
    session2.close();
  }
}
