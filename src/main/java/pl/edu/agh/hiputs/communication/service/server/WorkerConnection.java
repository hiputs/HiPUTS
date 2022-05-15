package pl.edu.agh.hiputs.communication.service.server;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.utils.MessageConverter;
import pl.edu.agh.hiputs.communication.model.messages.WorkerConnectionMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@Slf4j
public class WorkerConnection implements Runnable{

    private final Socket clientSocket;
    private final String workerId;

    @Getter
    private final int port;
    @Getter
    private final String address;
    private final MessagePropagationService messagePropagationService;
    private OutputStream outputStream;

    public WorkerConnection(Socket clientConnectionSocket, MessagePropagationService messagePropagationService, WorkerConnectionMessage workerConnectionMessage) {
        this.clientSocket = clientConnectionSocket;
        this.messagePropagationService = messagePropagationService;
        this.workerId = workerConnectionMessage.getWorkerId();
        port = workerConnectionMessage.getPort();
        address = workerConnectionMessage.getAddress();
    }

    @SneakyThrows
    @Override
    public void run() {
        InputStream inputStream = clientSocket.getInputStream();
        outputStream = clientSocket.getOutputStream();
        Message message = null;

        do {
            byte[] receivedMessage = inputStream.readAllBytes();
            message = MessageConverter.toMessage(receivedMessage);
            messagePropagationService.propagateMessage(message, workerId);
        } while (message.getMessageType() != MessagesTypeEnum.WorkerDisconnectMessage);
    }

    void send(Message message){
        try {
            byte[] encodedMsg = MessageConverter.toByteArray(message);
            outputStream.write(encodedMsg);
        } catch (IOException e) {
            log.error("Can not send message to workerId: " + workerId, e);
        }
    }
}
