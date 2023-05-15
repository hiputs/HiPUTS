package pl.edu.agh.hiputs.scheduler;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

public interface TaskExecutorService {

  void executeBatch(Collection<Runnable> task);

  List<?> executeCallableBatch(Collection<Callable<?>> tasks);
}
