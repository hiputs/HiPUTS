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
    public void waitForAllWorkers(MessagesTypeEnum state) {
        while(messageTypeWorkerRepository.get(state) == null || messageTypeWorkerRepository.get(state).size() == configurationService.getConfiguration().getWorkerCount()){
            try {
                wait();
            } catch (InterruptedException e) {
                log.error("Error while waiting for state: " + state, e);
            }
        }
    }

    @Override
    public void handleWorker(MessagesTypeEnum state, String workerId) {
        messageTypeWorkerRepository.putIfAbsent(state, new TreeSet<>())
                .add(workerId);
        notifyAll();

    }
}
