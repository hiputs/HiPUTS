package pl.edu.agh.hiputs.communication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.NeighbourConnection;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.NeighbourConnectionMessage;
import pl.edu.agh.hiputs.model.id.MapFragmentId;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.WorkerConnectionMessage;

@Service
@RequiredArgsConstructor
public class MessageSenderService implements Subscriber {

    private final Map<MapFragmentId, NeighbourConnection> neighbourRepository = new HashMap<>();
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
    public void send(MapFragmentId neighbourId, Message message) throws IOException {
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
        neighbourRepository.put(new MapFragmentId(workerConnectionMessage.getId()), connection);
    }
}
