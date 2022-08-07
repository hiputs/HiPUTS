package pl.edu.agh.hiputs.loadbalancer;

import java.util.List;
import pl.edu.agh.hiputs.loadbalancer.model.LoadBalancingHistoryInfo;

public interface LoadStatisticService {

  LoadBalancingHistoryInfo getMyLastLoad();

  List<LoadBalancingHistoryInfo> getMyLastLoads(int size);
}
