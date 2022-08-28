package pl.edu.agh.hiputs.loadbalancer;

import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import pl.edu.agh.hiputs.loadbalancer.model.LoadBalancingHistoryInfo;
import pl.edu.agh.hiputs.loadbalancer.model.SimulationPoint;
import pl.edu.agh.hiputs.service.worker.SimulationStatisticServiceImpl.LoadBalancingCostStatistic;

public interface LocalLoadStatisticService {

  LoadBalancingHistoryInfo getMyLastLoad();

  List<ImmutablePair<Integer, Long>> getAllByType(SimulationPoint loadBalancing);
}
