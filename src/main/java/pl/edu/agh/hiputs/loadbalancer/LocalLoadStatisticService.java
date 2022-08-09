package pl.edu.agh.hiputs.loadbalancer;

import java.util.List;
import pl.edu.agh.hiputs.loadbalancer.model.LoadBalancingHistoryInfo;

public interface LocalLoadStatisticService {

  LoadBalancingHistoryInfo getMyLastLoad();

  List<LoadBalancingHistoryInfo> getMyLastLoads(int size);
}
