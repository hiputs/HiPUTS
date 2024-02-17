package pl.edu.agh.hiputs.communication.service.server;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class WorkerRepository {

  private final Map<String, WorkerConnection> workerRepository = new ConcurrentHashMap<>();

  void addWorker(String workerId, WorkerConnection workerConnection) {
    workerRepository.put(workerId, workerConnection);
  }

  public WorkerConnection get(String workerId) {
    return workerRepository.get(workerId);
  }

  public int countWorker() {
    return workerRepository.size();
  }

  public Collection<String> getAllWorkersIds() {
    return getAll().stream().map(WorkerConnection::getWorkerId).collect(Collectors.toList());
  }

  Collection<WorkerConnection> getAll() {
    return workerRepository.values();
  }
}
