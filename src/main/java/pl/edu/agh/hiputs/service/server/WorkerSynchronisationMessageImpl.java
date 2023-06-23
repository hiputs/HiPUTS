package pl.edu.agh.hiputs.service.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.model.Configuration;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerSynchronisationMessageImpl implements WorkerSynchronisationService{

    private final Map<MessagesTypeEnum, Set<String>> messageTypeWorkerRepository = new HashMap<>();
    private final Configuration configuration;

    @Override
    public synchronized void waitForAllWorkers(MessagesTypeEnum state) {
        while(messageTypeWorkerRepository.get(state) == null || messageTypeWorkerRepository.get(state).size() < configuration.getWorkerCount()){
            try {
                wait();
            } catch (InterruptedException e) {
                log.error("Error while waiting for state: " + state, e);
            }
        }
    }

    @Override
    public synchronized void handleWorker(MessagesTypeEnum state, String workerId) {
        messageTypeWorkerRepository.putIfAbsent(state, new TreeSet<>());
        messageTypeWorkerRepository.get(state)
                .add(workerId);
        log.info(String.format("Server receive info -> Worker id: %s has reached the state %s, connection status %d / %d", workerId, state, messageTypeWorkerRepository.get(state).size(), configuration.getWorkerCount()));
        notifyAll();

    }
}
