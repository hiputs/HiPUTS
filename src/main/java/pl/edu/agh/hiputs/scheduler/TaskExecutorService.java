package pl.edu.agh.hiputs.scheduler;

import java.util.Collection;

public interface TaskExecutorService {

  void executeBatch(Collection<Runnable> task);
}
