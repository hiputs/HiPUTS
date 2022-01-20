package pl.edu.agh.scheduler;

public interface TaskReceptionUseCase {
    void addTask(Runnable task);

    void waitForAllTaskFinished();
}
