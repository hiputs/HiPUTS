package pl.edu.agh.scheduler;

import java.util.Collection;

public interface TaskExecutorService {
    void executeBatch(Collection<Runnable> task);
}
