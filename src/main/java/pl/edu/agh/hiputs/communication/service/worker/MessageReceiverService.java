package pl.edu.agh.hiputs.communication.service.worker;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

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
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.utils.MessageConverter;

/**
 * Socket where all messages addressed to the given client are received.
 */
@Slf4j
@Service
public class MessageReceiverService {

  private final Map<MessagesTypeEnum, List<Subscriber>> subscriberRepository = new HashMap<>();
  private final ExecutorService threadPoolExecutor = newSingleThreadExecutor();
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
    subscriberRepository.get(message.getMessageType()).forEach(subscriber -> subscriber.notify(message));
  }

  private class Listener implements Runnable {

    @Override
    public void run() {
      try {
        //toDo create simply server to get port for worker
        Random random = new Random();
        int portSeed = Math.abs(random.nextInt() % 40000);
        ServerSocket ss = null;

        while (true) {
          try {
            ss = new ServerSocket(10000 + portSeed);
            portSeed = Math.abs(random.nextInt() % 40000);
            break;
          } catch (Exception e) {
            log.warn("Port: " + portSeed + " is not available", e);
          }
        }
        port = ss.getLocalPort();
        while (true) {
          Socket s = ss.accept();
          threadPoolExecutor.submit(new SingleConnectionExecutor(s));
        }

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @RequiredArgsConstructor
  private class SingleConnectionExecutor implements Runnable {

    private final Socket clientSocket;

    @SneakyThrows
    @Override
    public void run() {
      byte[] receivedMessage = clientSocket.getInputStream().readAllBytes();
      Message message = MessageConverter.toMessage(receivedMessage);
      propagateMessage(message);
    }
  }
}
