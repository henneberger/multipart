package io.henneberger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.ServerWebSocket;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StreamingServer extends AbstractVerticle {

  private Set<Viewer> viewers = ConcurrentHashMap.newKeySet();

  @Override
  public void start() {
    HttpServerOptions options = new HttpServerOptions()
        .setMaxWebSocketFrameSize(10 * 1024 * 1024) // 10 MB
        .setMaxWebSocketMessageSize(10 * 1024 * 1024); // 10 MB
    HttpServer server = vertx.createHttpServer(options);

    server.webSocketHandler(socket -> {
      if (socket.path().equals("/stream")) {
        handleStreamSocket(socket);
      } else if (socket.path().equals("/watch")) {
        handleWatchSocket(socket);
      } else {
        socket.reject();
      }
    }).listen(8080, res -> {
      if (res.succeeded()) {
        System.out.println("Server is now listening on port 8080");
      } else {
        System.out.println("Failed to bind!");
      }
    });
  }

  private void handleStreamSocket(ServerWebSocket socket) {
    socket.frameHandler(frame -> {
      Buffer data = frame.binaryData();
      for (Viewer viewer : viewers) {
        viewer.sendFrame(data);
      }
    });
  }

  private void handleWatchSocket(ServerWebSocket socket) {
    Viewer viewer = new Viewer(socket);
    viewers.add(viewer);
    socket.closeHandler(v -> viewers.remove(viewer));
  }

  class Viewer {

    ServerWebSocket socket;
    boolean initialBufferSent = false;
    ConcurrentLinkedQueue<Buffer> pendingFrames = new ConcurrentLinkedQueue<>();
    boolean sending = false;

    Viewer(ServerWebSocket socket) {
      this.socket = socket;
    }

    void sendFrame(Buffer data) {
        writeData(data);
    }

    void sendInitialBuffer(Buffer buffer) {
      sending = true;
      socket.writeBinaryMessage(buffer, ar -> {
        if (ar.succeeded()) {
          initialBufferSent = true;
          // Send any pending frames
          flushPendingFrames();
        } else {
          System.err.println("Failed to send initial buffer to viewer: " + ar.cause());
        }
        sending = false;
      });
    }

    void flushPendingFrames() {
      while (!pendingFrames.isEmpty()) {
        Buffer frame = pendingFrames.poll();
        writeData(frame);
      }
    }

    void writeData(Buffer data) {
      socket.writeBinaryMessage(data);
    }
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new StreamingServer());
  }
}
