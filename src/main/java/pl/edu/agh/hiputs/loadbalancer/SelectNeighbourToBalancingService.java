package pl.edu.agh.hiputs.loadbalancer;

import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.LoadInfo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.messages.LoadInfoMessage;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.service.server.SubscriptionService;
import pl.edu.agh.hiputs.loadbalancer.model.BalancingMode;
import pl.edu.agh.hiputs.loadbalancer.model.LoadBalancingHistoryInfo;
import pl.edu.agh.hiputs.loadbalancer.utils.MapFragmentCostCalculatorUtil;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.service.ConfigurationService;

@Service
@RequiredArgsConstructor
public class SelectNeighbourToBalancingService implements Subscriber {

  private static final int MAX_AGE_DIFF = 10;

  @Getter
  private final Map<MapFragmentId, LoadBalancingHistoryInfo> loadRepository = new HashMap<>();
  private final SubscriptionService subscriptionService;
  private final TicketService ticketService;

  private int age = 0;

  public ImmutablePair<MapFragmentId, Double> selectNeighbourToBalancing(TransferDataHandler transferDataHandler, int age) {
    this.age = age;
    return ConfigurationService.getConfiguration().isTicketActive()
        ? getByTicket(transferDataHandler)
        : getLowesCost(transferDataHandler);
  }

  private ImmutablePair<MapFragmentId, Double> getByTicket(TransferDataHandler transferDataHandler) {
    ticketService.setActualStep(age);
    MapFragmentId candidate = ticketService.getActualTalker();

    if (candidate != null &&
        hasActualCostInfo(candidate) &&
        !transferDataHandler.getBorderPatches().get(candidate).isEmpty()) {
      return new ImmutablePair<MapFragmentId, Double>(candidate, calculateCost(candidate));
    }

    return null;
  }

  @PostConstruct
  void init() {
    if (ConfigurationService.getConfiguration().getBalancingMode() == BalancingMode.SIMPLY) {
      subscriptionService.subscribe(this, LoadInfo);
    }
  }

  private ImmutablePair<MapFragmentId, Double> getLowesCost(TransferDataHandler transferDataHandler){
    return transferDataHandler.getNeighbors()
        .parallelStream()
        .filter(this::hasActualCostInfo)
        .filter(id -> !transferDataHandler.getBorderPatches().get(id).isEmpty())
        .map(id -> new ImmutablePair<MapFragmentId, Double>(id, calculateCost(id)))
        .min(Comparator.comparingDouble(ImmutablePair::getRight))
        .orElse(null);
  }

  private boolean hasActualCostInfo(MapFragmentId mapFragmentId) {
    LoadBalancingHistoryInfo loadBalancingHistoryInfo = loadRepository.get(mapFragmentId);

    return loadBalancingHistoryInfo != null && loadBalancingHistoryInfo.getAge() + MAX_AGE_DIFF >= age;
  }

  private double calculateCost(MapFragmentId id) {
    LoadBalancingHistoryInfo info = loadRepository.get(id);
    return MapFragmentCostCalculatorUtil.calculateCost(info);
  }


  @Override
  public void notify(Message message) {
    addToLoadBalancing((LoadInfoMessage) message);
  }

  private void addToLoadBalancing(LoadInfoMessage message) {
    MapFragmentId id = new MapFragmentId(message.getMapFragmentId());

    loadRepository.put(id, new LoadBalancingHistoryInfo(message.getCarCost(), message.getTime(),  0L, message.getMapCost(), age));
  }

}
