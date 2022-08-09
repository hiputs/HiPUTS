package pl.edu.agh.hiputs.loadbalancer;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.loadbalancer.model.LoadBalancingHistoryInfo;

@Service
@RequiredArgsConstructor
public class LocalLoadStatisticServiceImpl implements LocalLoadStatisticService{

  @Override
  public LoadBalancingHistoryInfo getMyLastLoad() {
    return null;
  }

  @Override
  public List<LoadBalancingHistoryInfo> getMyLastLoads(int size) {
    return null;
  }
}
