package pl.edu.agh.hiputs.communication.service.worker;

import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.io.IOException;
import java.io.ObjectInputStream;
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
    log.info("Worker receive message: " + message.getMessageType());
    subscriberRepository.get(message.getMessageType()).forEach(subscriber -> subscriber.notify(message));
  }

  private class Listener implements Runnable {

    @Override
    public void run() {
      try {
        //toDo create simply server to get port for worker
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
      }
    }
  }

  @RequiredArgsConstructor
  private class SingleConnectionExecutor implements Runnable {

    private final Socket clientSocket;

    @Override
    public void run() {
      ObjectInputStream objectInputStream = createObjectInputStreamOrThrowException();

      while(true){
        Message message = readMessageOrThrowException(objectInputStream);
        propagateMessage(message);
      }

    }

    private ObjectInputStream createObjectInputStreamOrThrowException() {
      try {
        return new ObjectInputStream(clientSocket.getInputStream());
      } catch (IOException | NullPointerException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

    private Message readMessageOrThrowException(ObjectInputStream objectInputStream) {
      try {
        return (Message) objectInputStream.readObject();
      } catch (IOException | ClassNotFoundException | NullPointerException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }
  }
}
