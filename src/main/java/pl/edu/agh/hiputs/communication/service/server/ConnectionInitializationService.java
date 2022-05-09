package pl.edu.agh.hiputs.communication.service.server;

import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.WorkerConnectionMessage;
import pl.edu.agh.hiputs.communication.utils.MessageConverter;

/**
 * Socket where all messages addressed to the given client are received.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionInitializationService {

    private final ExecutorService threadPoolExecutor = newCachedThreadPool();
    private final ExecutorService listenerExecutor = newSingleThreadExecutor();
    private final MessagePropagationService messagePropagationService;
    private final WorkerRepository workerRepository;


    @PostConstruct
    private void initSocket() {
        listenerExecutor.submit(new Listener());
    }

    private class Listener implements Runnable {

        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(11000);

                while (true) {
                    try {
                        Socket clientConnectionSocket = serverSocket.accept();
                        WorkerConnectionMessage workerConnectionMessage = getWorkerConnectionMessage(clientConnectionSocket);
                        WorkerConnection workerConnection = new WorkerConnection(clientConnectionSocket, messagePropagationService, workerConnectionMessage);

                        workerRepository.addWorker(workerConnectionMessage.getWorkerId(), workerConnection);
                        threadPoolExecutor.submit(workerConnection);
                    }
                    catch (Exception e) {
                        log.error("Fail create connection with worker");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private WorkerConnectionMessage getWorkerConnectionMessage(Socket clientConnectionSocket) throws IOException, ClassNotFoundException {
                byte[] receivedMessage = clientConnectionSocket.getInputStream().readAllBytes();
                Message message = MessageConverter.toMessage(receivedMessage);
                WorkerConnectionMessage workerConnectionMessage = (WorkerConnectionMessage) message;
                log.info("Connect with worker: " + workerConnectionMessage.getWorkerId());
                return workerConnectionMessage;
        }

    }
}
