package pl.edu.agh.hiputs.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SchedulerServiceTest {

  @Autowired
  private TaskExecutorService taskReceptionUseCase;

  @Test
  void createFiveTaskAndWaitForIt() {

    //given
    AtomicInteger counter = new AtomicInteger();

    List<Runnable> tasks =
        List.of(new PrimitiveTask(counter, 1), new PrimitiveTask(counter, 2), new PrimitiveTask(counter, 3),
            new PrimitiveTask(counter, 4), new PrimitiveTask(counter, 5));

    //when
    taskReceptionUseCase.executeBatch(tasks);

    //then
    assertEquals(5, counter.get());
  }

  @RequiredArgsConstructor
  private class PrimitiveTask implements Runnable {

    private final AtomicInteger counter;
    private final int seed;

    @SneakyThrows
    @Override
    public void run() {
      Random random = new Random(seed);
      Thread.sleep(1000 + random.nextInt() % 1000);
      counter.incrementAndGet();
    }
  }
}
