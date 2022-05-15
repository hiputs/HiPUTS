package pl.edu.agh.hiputs.service.server;

import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

public interface WorkerSynchronisationService {

    void waitForAllWorkers(MessagesTypeEnum state);

    void handleWorker(MessagesTypeEnum state, String workerId);
}
