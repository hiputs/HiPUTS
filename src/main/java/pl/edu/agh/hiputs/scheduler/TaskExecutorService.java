package pl.edu.agh.hiputs.scheduler;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface TaskExecutorService {

  void executeBatch(Collection<Runnable> task);

  List<?> executeCallableBatch(Collection<Callable<?>> tasks);

  List<Future<?>> executeBatchReturnFutures(Collection<Runnable> tasks);

  void waitForAllTaskFinished(List<Future<?>> futures);
}
