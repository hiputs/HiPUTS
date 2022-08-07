package pl.edu.agh.hiputs.loadbalancer;

import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.LoadInfo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.messages.LoadInfoMessage;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.service.worker.SubscriptionService;
import pl.edu.agh.hiputs.loadbalancer.model.BalancingMode;
import pl.edu.agh.hiputs.loadbalancer.model.LoadBalancingHistoryInfo;
import pl.edu.agh.hiputs.loadbalancer.utils.MapFragmentCostCalculatorUtil;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.service.ConfigurationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimplyLoadBalancingService implements LoadBalancingStrategy, Subscriber {

  private static final int MAX_AGE_DIFF = 10;
  private static final double ALLOW_LOAD_IMBALANCE = 1.03;
  private final SubscriptionService subscriptionService;
  private final ConfigurationService configurationService;

  private final LoadStatisticService loadStatisticService;
  private final Map<MapFragmentId, LoadBalancingHistoryInfo> loadRepository = new HashMap<>();
  private int age = 0;

  @PostConstruct
  void init(){
    if(configurationService.getConfiguration().getBalancingMode() == BalancingMode.SIMPLY){
      subscriptionService.subscribe(this, LoadInfo);
    }
  }

  @Override
  public void notify(Message message) {
    addToLoadBalancing((LoadInfoMessage) message);
  }

  private void addToLoadBalancing(LoadInfoMessage message) {
    MapFragmentId id = new MapFragmentId(message.getMapFragmentId());

    loadRepository.put(id, new LoadBalancingHistoryInfo(message.getCarCost(), message.getTime(), age));
  }

  @Override
  public LoadBalancingDecision makeBalancingDecision(TransferDataHandler transferDataHandler) {
    LoadBalancingDecision loadBalancingDecision = new LoadBalancingDecision();

    LoadBalancingHistoryInfo info = loadStatisticService.getMyLastLoad();
    ImmutablePair<MapFragmentId, Double> candidate = selectNeighbourToBalancing(transferDataHandler);

    boolean shouldBalancing = calculateCost(info) > candidate.getRight() * ALLOW_LOAD_IMBALANCE;
    age++;
    loadBalancingDecision.setLoadBalancingRecommended(shouldBalancing);

    if(!shouldBalancing){
      return loadBalancingDecision;
    }

    loadBalancingDecision.setSelectedNeighbour(candidate.getLeft());
    loadBalancingDecision.setCarImbalanceRate(info.getCarCost() - loadRepository.get(candidate.getLeft()).getCarCost());

    return loadBalancingDecision;
  }

  private ImmutablePair<MapFragmentId, Double> selectNeighbourToBalancing(TransferDataHandler transferDataHandler) {

    return transferDataHandler.getNeighbors()
        .stream()
        .filter(this::hasActualCostInfo)
        .map(id -> new ImmutablePair<MapFragmentId, Double>(id, calculateCost(id)))
        .min(Comparator.comparingDouble(ImmutablePair::getRight))
        .get();
  }

  private double calculateCost(MapFragmentId id) {
    return calculateCost(loadRepository.get(id));
  }

  private double calculateCost(LoadBalancingHistoryInfo info) {
    return MapFragmentCostCalculatorUtil.calculateCost(info);
  }

  private boolean hasActualCostInfo(MapFragmentId mapFragmentId) {
    LoadBalancingHistoryInfo loadBalancingHistoryInfo = loadRepository.get(mapFragmentId);

    return loadBalancingHistoryInfo != null && loadBalancingHistoryInfo.getAge() + MAX_AGE_DIFF >= age;
  }
}
