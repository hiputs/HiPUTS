package pl.edu.agh.communication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.communication.NeighbourConnection;
import pl.edu.agh.communication.Subscriber;
import pl.edu.agh.communication.model.messages.Message;
import pl.edu.agh.communication.model.messages.NeighbourConnectionMessage;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static pl.edu.agh.communication.model.MessagesTypeEnum.WorkerConnectionMessage;

@Service
@RequiredArgsConstructor
public class MessageSenderService implements Subscriber {

    private final Map<String, NeighbourConnection> neighbourRepository = new HashMap<>();
    private final SubscriptionService subscriptionService;

    @PostConstruct
    void init() {
        subscriptionService.subscribe(this, WorkerConnectionMessage);
    }

    /**
     * @param neighbourId - unique worker id
     * @param message     - message to send
     * @throws IOException <p>Method send message to specific client</p>
     */
    public void send(String neighbourId, Message message) throws IOException {
        neighbourRepository.get(neighbourId)
                .send(message);
    }

    /**
     * @param message - message to send
     *
     *                <p>Method send message to all existing worker</p>
     */
    public void broadcast(Message message) {
        neighbourRepository.values()
                .forEach(n -> {
                    try {
                        n.send(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void notify(Message message) {
        NeighbourConnectionMessage workerConnectionMessage = (NeighbourConnectionMessage) message;
        NeighbourConnection connection = new NeighbourConnection(workerConnectionMessage);
        neighbourRepository.put(workerConnectionMessage.getId(), connection);
    }
}
