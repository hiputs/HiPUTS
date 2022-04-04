package pl.edu.agh.hiputs.communication.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.utils.MessageConverter;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * Socket where all messages addressed to the given client are received.
 */
@Service
public class MessageReceiverService {

    private final Map<MessagesTypeEnum, List<Subscriber>> subscriberRepository = new HashMap<>();
    private final ExecutorService threadPoolExecutor = newSingleThreadExecutor();
    private final ExecutorService listenerExecutor = newSingleThreadExecutor();

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
        subscriberRepository.get(message.getMessageType()).forEach(
                subscriber -> subscriber.notify(message)
        );
    }

    private class Listener implements Runnable {

        @Override
        public void run() {
            try {
                //toDo create simply server to get port for worker
                ServerSocket ss = new ServerSocket(6666);

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
