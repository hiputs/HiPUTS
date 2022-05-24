package pl.edu.agh.hiputs.communication.service.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.WorkerConnectionMessage;

import java.io.IOException;
import java.net.Socket;

@Slf4j
public class WorkerConnection implements Runnable{

    private final String workerId;

    @Getter
    private final int port;
    @Getter
    private final String address;
    private final MessagePropagationService messagePropagationService;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;

    public WorkerConnection(ObjectInputStream inputStream, MessagePropagationService messagePropagationService, WorkerConnectionMessage workerConnectionMessage)
        throws IOException {
        this.messagePropagationService = messagePropagationService;
        this.workerId = workerConnectionMessage.getWorkerId();
        port = workerConnectionMessage.getPort();
        address = workerConnectionMessage.getAddress();

        this.inputStream = inputStream;
        Socket socket = new Socket(address, port);
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            Message message = null;

            do {
                message = (Message) inputStream.readObject();
                messagePropagationService.propagateMessage(message, workerId);
            } while (message.getMessageType() != MessagesTypeEnum.WorkerDisconnectMessage);
        } catch (Exception e){
            log.error("Fail messageHandler for worker id: " + workerId, e);
        }
    }

    void send(Message message){
        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            log.error("Can not send message to workerId: " + workerId, e);
        }
    }
}
