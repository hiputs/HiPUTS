package pl.edu.agh.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class SchedulerServiceTest {

    @Autowired
    private TaskReceptionUseCase taskReceptionUseCase;

    @Test
    void createFiveTaskAndNotWaitForIt(){

        //given
        AtomicInteger counter = new AtomicInteger();
        PrimitiveTask primitiveTask1 = new PrimitiveTask(counter, 1);
        PrimitiveTask primitiveTask2 = new PrimitiveTask(counter, 2);
        PrimitiveTask primitiveTask3 = new PrimitiveTask(counter, 3);
        PrimitiveTask primitiveTask4 = new PrimitiveTask(counter, 4);
        PrimitiveTask primitiveTask5 = new PrimitiveTask(counter, 5);

        //when
        taskReceptionUseCase.addTask(primitiveTask1);
        taskReceptionUseCase.addTask(primitiveTask2);
        taskReceptionUseCase.addTask(primitiveTask3);
        taskReceptionUseCase.addTask(primitiveTask4);
        taskReceptionUseCase.addTask(primitiveTask5);

        //then
        assertEquals(0, counter.get());
    }

    @Test
    void createFiveTaskAndWaitForIt(){

        //given
        AtomicInteger counter = new AtomicInteger();
        PrimitiveTask primitiveTask1 = new PrimitiveTask(counter, 1);
        PrimitiveTask primitiveTask2 = new PrimitiveTask(counter, 2);
        PrimitiveTask primitiveTask3 = new PrimitiveTask(counter, 3);
        PrimitiveTask primitiveTask4 = new PrimitiveTask(counter, 4);
        PrimitiveTask primitiveTask5 = new PrimitiveTask(counter, 5);

        //when
        taskReceptionUseCase.addTask(primitiveTask1);
        taskReceptionUseCase.addTask(primitiveTask2);
        taskReceptionUseCase.addTask(primitiveTask3);
        taskReceptionUseCase.addTask(primitiveTask4);
        taskReceptionUseCase.addTask(primitiveTask5);

        taskReceptionUseCase.waitForAllTaskFinished();

        //then
        assertEquals(5, counter.get());
    }


    @RequiredArgsConstructor
    private class PrimitiveTask implements Runnable{
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
