package pl.edu.agh.hiputs.service.pathfinder;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@ExtendWith(MockitoExtension.class)
public class CHBidirectionalDijkstraTest {
    @Mock
    private MapRepository mapRepository;

    @SneakyThrows
    @Test
    void createSerializeAndDeserializePrecomputations() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        CHBidirectionalDijkstra bidirectionalDijkstra = new CHBidirectionalDijkstra(mapRepository, executor);
        bidirectionalDijkstra.dumpPrecomputationToFile("dump.obj");
        CHBidirectionalDijkstra copyBidirectionalDijkstra = new CHBidirectionalDijkstra("dump.obj");
    }

    @Test
    void pathfinderSanityTest() {
        MapRepository mapRepository;
    }
}