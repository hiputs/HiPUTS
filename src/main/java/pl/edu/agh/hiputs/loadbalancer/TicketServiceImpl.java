package pl.edu.agh.hiputs.loadbalancer;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.AvailableTicketMessage;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.SelectTicketMessage;
import pl.edu.agh.hiputs.communication.model.messages.ServerInitializationMessage;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.communication.service.worker.WorkerSubscriptionService;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.service.ConfigurationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService, Subscriber {

  private int TICKET_POOL_SIZE = 10;
  private final WorkerSubscriptionService subscriptionService;
  private final MessageSenderService messageSenderService;

  private final ConfigurationService configurationService;


  private MapFragmentId[] ticketPool;

  private final Queue<AvailableTicketMessage> availableTicketMessageQueue = new LinkedList<>();
  private final Queue<MapFragmentId> newMapFragment = new LinkedList<>();
  private final BlockingQueue<SelectTicketMessage> selectTicketQueue = new LinkedBlockingQueue<>();

  private final Executor executor = Executors.newSingleThreadExecutor();

  private final AtomicInteger actualStep = new AtomicInteger();

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.ServerInitializationMessage);
    subscriptionService.subscribe(this, MessagesTypeEnum.SelectTicketMessage);
    subscriptionService.subscribe(this, MessagesTypeEnum.AvailableTicketMessage);

    TICKET_POOL_SIZE = Math.min(TICKET_POOL_SIZE, configurationService.getConfiguration().getWorkerCount() * 2);
    ticketPool = new MapFragmentId[TICKET_POOL_SIZE];
    executor.execute(new TicketInfinityLoop());
  }

  @Override
  public synchronized void notify(Message message) {
    switch (message.getMessageType()) {
      case SelectTicketMessage -> selectTicketQueue.offer((SelectTicketMessage) message);
      case AvailableTicketMessage -> availableTicketMessageQueue.offer((AvailableTicketMessage) message);
      case ServerInitializationMessage -> {
        ServerInitializationMessage initializationMessage = (ServerInitializationMessage) message;
        final List<MapFragmentId> mapFragmentIds = initializationMessage.getWorkerInfo()
            .stream()
            .map(w -> new MapFragmentId(w.getConnectionData().getId()))
            .toList();

        newMapFragment.addAll(mapFragmentIds);
      }
    }

    this.notify();
  }

  @Override
  public void setActualStep(int step) {
    actualStep.set(step);
  }

  @Override
  public MapFragmentId getActualTalker() {
    return ticketPool[actualStep.get() % TICKET_POOL_SIZE];
  }

  @Override
  public synchronized void addNewTalker(MapFragmentId neighbourId) {
    newMapFragment.add(neighbourId);
    notify();
  }

  @Override
  public void removeTalker(MapFragmentId neighbourId) {
      for(int i=0; i< TICKET_POOL_SIZE; i++){
        if(neighbourId.equals(ticketPool[i])){
          ticketPool[i] = null;
        }
      }
  }

  private class TicketInfinityLoop implements Runnable {

    @Override
    public void run() {
      while (true) {
        if (!availableTicketMessageQueue.isEmpty()) {
          selectTicket();
          continue;
        }

        if(!newMapFragment.isEmpty()){
          MapFragmentId neighbour =  newMapFragment.remove();

          if(hasAlreadyTicket(neighbour)){
            continue;
          }

          if(neighbour.hashCode() > configurationService.getConfiguration().getMapFragmentId().hashCode()){// wait for message because older send theirs possibility
            continue;
          }

          startSyncTicketNegotiaton(neighbour);
        } else {
          waitingZone();
        }
      }

    }

    private void startSyncTicketNegotiaton(MapFragmentId neighbour) {
      try {
        MapFragmentId me = configurationService.getConfiguration().getMapFragmentId();
      //  log.info("Start negotiation between {} and me {}", neighbour.getId(), me.getId());
        messageSenderService.send(neighbour, new AvailableTicketMessage(me.getId(), getFreeTicket()));
        final SelectTicketMessage selectTicketMessage = selectTicketQueue.take();
        setTicket(selectTicketMessage.getTicket(), new MapFragmentId(selectTicketMessage.getMapFragmentId()));
      } catch (Exception e) {
        log.error("Send free ticket error", e);
      }
    }

    private void selectTicket() {
      AvailableTicketMessage message = availableTicketMessageQueue.poll();
      int selectTicket = getTicket(message);
      MapFragmentId neighbour = new MapFragmentId(message.getMapFragmentId());
      try {
        //log.info("Select ticket {} neighbour {}", selectTicket, neighbour.getId());
        messageSenderService.send(neighbour, new SelectTicketMessage(configurationService.getConfiguration().getMapFragmentId().getId(), selectTicket));
      } catch (IOException e) {
        log.error("Send message error");
      }

      setTicket(selectTicket, neighbour);
    }

    private void setTicket(int ticket, MapFragmentId mapFragmentId) {
      if (ticketPool[ticket] != null) {
        log.error("Override ticket {}", ticket);
      }
      //log.info("Worker {} get token {}", mapFragmentId, ticket);
      ticketPool[ticket] = mapFragmentId;
    }
  }

  private boolean hasAlreadyTicket(MapFragmentId neighbour) {
    return Arrays.asList(ticketPool).contains(neighbour);
  }

  private List<Integer> getFreeTicket() {
    List<Integer> free = new LinkedList<>();

    for(int i = 0; i< TICKET_POOL_SIZE; i++){
      if(ticketPool[i] == null){
        free.add(i);
      }
    }

    return free;
  }

  private int getTicket(AvailableTicketMessage message) {
    final Optional<Integer> ticket =
        message.getFreeTicket().stream().filter(i -> i % 2 == 0).filter(i -> ticketPool[i] == null).findFirst();

    return ticket.orElseGet(
        () -> message.getFreeTicket().stream().filter(i -> ticketPool[i] == null).findFirst().get());
  }

  private synchronized void waitingZone(){
    try {
      this.wait();
    } catch (InterruptedException e) {
      log.error("wait ticket execption", e);
    }
  }

}
