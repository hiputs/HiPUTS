package pl.edu.agh.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.communication.Subscriber;
import pl.edu.agh.communication.model.MessagesTypeEnum;
import pl.edu.agh.communication.model.messages.CarTransferMessage;
import pl.edu.agh.communication.model.messages.Message;
import pl.edu.agh.communication.model.serializable.SCar;
import pl.edu.agh.communication.service.MessageSenderService;
import pl.edu.agh.communication.service.SubscriptionService;
import pl.edu.agh.model.actor.MapFragment;
import pl.edu.agh.model.id.ActorId;
import pl.edu.agh.model.id.PatchId;
import pl.edu.agh.model.map.Patch;
import pl.edu.agh.scheduler.TaskExecutorService;
import pl.edu.agh.scheduler.task.CarMapperTask;
import pl.edu.agh.scheduler.task.InjectIncomingCarsTask;
import pl.edu.agh.service.usecase.CarSynchronizedService;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CarSynchronizedServiceImpl implements CarSynchronizedService, Subscriber {

    private final MapFragment mapFragment;
    private final SubscriptionService subscriptionService;
    private final TaskExecutorService taskExecutorUseCase;
    private final MessageSenderService messageSenderService;

    private final List<CarTransferMessage> incomingMessages = new ArrayList<>();
    private final List<CarTransferMessage> featureIncomingMessages = new ArrayList<>();

    @PostConstruct
    void init() {
        subscriptionService.subscribe(this, MessagesTypeEnum.CarTransferMessage);
    }

    @Override
    public void sendCarsToNeighbours() {
        Map<ActorId, LinkedList<SCar>> serializedCarMap = new HashMap<>();
        List<Runnable> tasks = new ArrayList<>();
        Map<PatchId, Patch> remotePatches = mapFragment.getBorderPatches();

        for (Map.Entry<PatchId, ActorId> entry : mapFragment.getPatch2Actor().entrySet()) {
            LinkedList<SCar> toSendCars = serializedCarMap.computeIfAbsent(entry.getValue(), k -> new LinkedList<>());

            tasks.add(new CarMapperTask(remotePatches.get(entry.getKey()), toSendCars));
        }

        taskExecutorUseCase.executeBatch(tasks);
        sendMessages(serializedCarMap);
    }

    private void sendMessages(Map<ActorId, LinkedList<SCar>> serializedCarMap) {
        for (Map.Entry<ActorId, LinkedList<SCar>> entry : serializedCarMap.entrySet()) {
            CarTransferMessage carTransferMessage = new CarTransferMessage(entry.getValue());
            try {
                messageSenderService.send(entry.getKey(), carTransferMessage);
            } catch (IOException e) {
                log.error("Error sending message CarTransferMessage to: " + entry.getKey(), e);
            }
        }
    }

    @SneakyThrows
    @Override
    public void synchronizedGetIncomingCar() {
        int countOfNeighbours = mapFragment.getNeighbours().size();
        while (incomingMessages.size() < countOfNeighbours) {
            this.wait();
        }

        List<Runnable> injectIncomingCarTasks = incomingMessages
                .stream()
                .map(message -> new InjectIncomingCarsTask(message.getCars(), mapFragment))
                .collect(Collectors.toList());

        taskExecutorUseCase.executeBatch(injectIncomingCarTasks);

        incomingMessages.clear();
        incomingMessages.addAll(featureIncomingMessages);
        featureIncomingMessages.clear();
    }

    @Override
    public void notify(Message message) {
        if (message.getMessageType() != MessagesTypeEnum.CarTransferMessage) {
            return;
        }

        CarTransferMessage carTransferMessage = (CarTransferMessage) message;
        if (incomingMessages.contains(carTransferMessage)) {
            featureIncomingMessages.add(carTransferMessage);
        } else {
            incomingMessages.add(carTransferMessage);
        }

        notifyAll();
    }
}
