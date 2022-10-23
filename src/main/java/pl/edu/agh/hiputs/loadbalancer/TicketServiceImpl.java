package pl.edu.agh.hiputs.loadbalancer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.AvailableTicketMessage;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.SelectTicketMessage;
import pl.edu.agh.hiputs.communication.model.messages.WorkerConnectionMessage;
import pl.edu.agh.hiputs.communication.service.worker.SubscriptionService;
import pl.edu.agh.hiputs.model.id.MapFragmentId;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TickerCheckerService, Subscriber {

  private static final int TICKET_POOL_SIZE = 10;
  private final SubscriptionService subscriptionService;

  private final ArrayList<MapFragmentId> ticketPool = new ArrayList<>(TICKET_POOL_SIZE);

  private final Queue<AvailableTicketMessage> availableTicketMessageQueue = new LinkedList<>();
  private final Queue<MapFragmentId> newMapFragment = new LinkedList<>();
  private final BlockingQueue<SelectTicketMessage> selectTicketQueue = new LinkedBlockingQueue<>();

  private final Executor executor = Executors.newSingleThreadExecutor();

  private final AtomicInteger actualStep = new AtomicInteger();

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.WorkerConnectionMessage);
    subscriptionService.subscribe(this, MessagesTypeEnum.SelectTicketMessage);
    subscriptionService.subscribe(this, MessagesTypeEnum.AvailableTicketMessage);
  }

  @Override
  public void notify(Message message) {
    switch (message.getMessageType()){
      case SelectTicketMessage -> selectTicketQueue.offer((SelectTicketMessage) message);
      case AvailableTicketMessage -> availableTicketMessageQueue.offer((AvailableTicketMessage) message);
      case WorkerConnectionMessage -> newMapFragment.offer(new MapFragmentId(((WorkerConnectionMessage) message).getWorkerId()));
    }
  }

  @Override
  public void setActualStep(int step) {
    actualStep.set(step);
  }

  @Override
  public MapFragmentId getActualTalker() {
    return ticketPool.get(actualStep.get());
  }


}
