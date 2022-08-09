package pl.edu.agh.hiputs.communication.service.server;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.model.messages.WorkerConnectionMessage;
import pl.edu.agh.hiputs.service.ConfigurationService;

/**
 * Socket where all messages addressed to the given client are received.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionInitializationService {

  private final ExecutorService listenerExecutor = newSingleThreadExecutor();
  private final MessagePropagationService messagePropagationService;
  private final WorkerRepository workerRepository;
  private final ConfigurationService configurationService;

  public void init() {
    listenerExecutor.submit(new Listener());
  }

  private class Listener implements Runnable {

    @Override
    public void run() {
      try {
        ThreadPoolExecutor connectionExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        ServerSocket serverSocket = new ServerSocket(configurationService.getConfiguration().getServerPort());
        if (serverSocket.isClosed()) {
          log.error("Server fail");
        } else {
          log.info("Server listening on port: " + serverSocket.getLocalPort());
        }

        while (true) {
          try {
            Socket clientConnectionSocket = serverSocket.accept();
            connectionExecutor.submit(new ConnectionInitializationHandler(clientConnectionSocket, connectionExecutor));
          } catch (Exception e) {
            log.error("Fail create connection with worker");
          }
        }
      } catch (IOException e) {
        log.error("IOException occurred", e);
      } catch (Exception e) {
        log.error("Unexpected exception occurred", e);
      }
    }
  }

  @RequiredArgsConstructor
  private class ConnectionInitializationHandler implements Runnable {

    private final Socket clientConnectionSocket;
    private final ExecutorService connectionExecutor;

    @Override
    public void run() {
      try {
        log.info(String.format("New connection from: %s:%s", clientConnectionSocket.getInetAddress().getHostAddress(),
            clientConnectionSocket.getPort()));

        ObjectInputStream input = new ObjectInputStream(clientConnectionSocket.getInputStream());
        WorkerConnectionMessage workerConnectionMessage = getWorkerConnectionMessage(input);

        WorkerConnection workerConnection =
            new WorkerConnection(input, messagePropagationService, workerConnectionMessage);
        connectionExecutor.submit(workerConnection);
        workerRepository.addWorker(workerConnectionMessage.getWorkerId(), workerConnection);
        messagePropagationService.propagateMessage(workerConnectionMessage, workerConnectionMessage.getWorkerId());
      } catch (Exception exception) {
        log.error(String.format("Error during initialization connection with: %s:%s",
            clientConnectionSocket.getInetAddress().getHostAddress(), clientConnectionSocket.getPort()), exception);
      }
    }

    private WorkerConnectionMessage getWorkerConnectionMessage(ObjectInputStream objectInputStream)
        throws IOException, ClassNotFoundException {
      return (WorkerConnectionMessage) objectInputStream.readObject();
    }
  }
}
