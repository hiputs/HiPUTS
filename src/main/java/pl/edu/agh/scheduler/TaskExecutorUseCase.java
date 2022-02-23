package pl.edu.agh.scheduler;

import java.util.Collection;

public interface TaskExecutorUseCase {
    void executeBatch(Collection<Runnable> task);
}
