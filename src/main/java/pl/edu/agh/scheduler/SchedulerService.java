package pl.edu.agh.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.scheduler.exception.InsufficientSystemResourcesException;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Service
public class SchedulerService implements TaskReceptionUseCase{

    private ForkJoinPool threadPool;
    private final List<Future<?>> futures = new LinkedList<>();
    private static final int RESERVED_THREADS_NUMBER = 1;

    @PostConstruct
    void init() {
        int cores = getFreeCore();

        if (cores <= 0) {
            throw new InsufficientSystemResourcesException("Insufficient number of cores");
        }
        threadPool = new ForkJoinPool(cores);
    }

    private int getFreeCore() {
        return Runtime.getRuntime().availableProcessors() - RESERVED_THREADS_NUMBER;
    }

    public void addTask(Runnable task) {
        futures.add(threadPool.submit(task));
    }

    @Override
    public void waitForAllTaskFinished() {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error occurred util waiting for task", e);
            }
        }
    }
}
