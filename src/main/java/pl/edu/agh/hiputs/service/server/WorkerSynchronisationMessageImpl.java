package pl.edu.agh.hiputs.service.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

import java.util.*;
import pl.edu.agh.hiputs.service.ConfigurationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerSynchronisationMessageImpl implements WorkerSynchronisationService{

    private final Map<MessagesTypeEnum, Set<String>> messageTypeWorkerRepository = new HashMap<>();
    private final ConfigurationService configurationService;

    @Override
    public synchronized void waitForAllWorkers(MessagesTypeEnum state) {
        while(messageTypeWorkerRepository.get(state) == null || messageTypeWorkerRepository.get(state).size() < configurationService.getConfiguration().getWorkerCount()){
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
        log.info(String.format("Worker id: %s connection with server, connection status %d / %d", workerId, messageTypeWorkerRepository.get(state).size(), configurationService.getConfiguration().getWorkerCount()));
        notifyAll();

    }
}
