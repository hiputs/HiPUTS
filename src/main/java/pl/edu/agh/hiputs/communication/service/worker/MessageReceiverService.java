package pl.edu.agh.hiputs.communication.service.worker;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;

/**
 * Socket where all messages addressed to the given client are received.
 */
@Slf4j
@Service
public class MessageReceiverService {

  private final Map<MessagesTypeEnum, List<Subscriber>> subscriberRepository = new HashMap<>();
  private final ThreadPoolExecutor connectionExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
  private final ExecutorService listenerExecutor = newSingleThreadExecutor();
  @Getter
  private int port;

  public MessageReceiverService() {
    Arrays.stream(MessagesTypeEnum.values())
        .forEach(messagesType -> subscriberRepository.put(messagesType, new LinkedList<>()));
  }

  @PostConstruct
  private void initSocket() {
    listenerExecutor.submit(new Listener());
  }

  public void addNewSubscriber(Subscriber subscriber, MessagesTypeEnum messagesEnum) {
    subscriberRepository.get(messagesEnum).add(subscriber);
  }

  public void propagateMessage(Message message) {
    log.debug("Worker receive message: {}", message.getMessageType());
    subscriberRepository.get(message.getMessageType()).forEach(subscriber -> subscriber.notify(message));
  }

  private class Listener implements Runnable {

    @Override
    public void run() {
      try {
        Random random = new Random();
        int portSeed = 10000 + Math.abs(random.nextInt() % 40000);
        ServerSocket ss = null;

        while (true) {
          try {
            ss = new ServerSocket(portSeed);
            portSeed = 10000 + Math.abs(random.nextInt() % 40000);
            break;
          } catch (Exception e) {
            log.warn("Port: " + portSeed + " is not available", e);
          }
        }
        port = ss.getLocalPort();
        while (true) {
          Socket s = ss.accept();
          connectionExecutor.submit(new SingleConnectionExecutor(s));
        }

      } catch (IOException e) {
        e.printStackTrace();
      } catch (Exception e) {
        log.error("Unexpected exception occurred", e);
      }
    }
  }

  @RequiredArgsConstructor
  private class SingleConnectionExecutor implements Runnable {

    private final Socket clientSocket;

    @Override
    public void run() {
      try {
        DataInputStream dataInputStream = createDataInputStreamOrThrowException();

        while (true) {
          Message message = readMessageOrThrowException(dataInputStream);
          propagateMessage(message);
        }
      } catch (RuntimeException e) {
        log.error("Exception occurred in message handling thread", e);
      }

    }

    private DataInputStream createDataInputStreamOrThrowException() {
      try {
        return new DataInputStream(clientSocket.getInputStream());
      } catch (IOException | NullPointerException e) {
        log.error("ERROR ", e);
        throw new RuntimeException(e);
      }
    }

    private Message readMessageOrThrowException(DataInputStream dataInputStream) {
      try {
        int length = dataInputStream.readInt();
        byte[] bytes = dataInputStream.readNBytes(length);
        return SerializationUtils.deserialize(bytes);
      } catch (IOException | NullPointerException e) {
        log.error("ERROR ", e);
        throw new RuntimeException(e);
      }
    }
  }
}
