package pl.edu.agh.hiputs.loadbalancer;

import pl.edu.agh.hiputs.loadbalancer.model.LoadBalancingHistoryInfo;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;

public interface LocalLoadMonitorService {

  void init(TransferDataHandler transferDataHandler);

  LoadBalancingHistoryInfo getMyLastLoad(int step);

  void notifyAboutMyLoad(int step);

}