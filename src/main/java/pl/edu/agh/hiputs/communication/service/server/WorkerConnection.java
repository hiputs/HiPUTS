package pl.edu.agh.hiputs.communication.service.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.WorkerConnectionMessage;

@Slf4j
public class WorkerConnection implements Runnable {

    @Getter
    private final String workerId;
    @Getter
    private final int port;
    @Getter
    private final String address;
    private final MessagePropagationService messagePropagationService;
    private final DataOutputStream outputStream;
    private final DataInputStream inputStream;

    public WorkerConnection(DataInputStream inputStream, MessagePropagationService messagePropagationService, WorkerConnectionMessage workerConnectionMessage)
        throws IOException {
        this.messagePropagationService = messagePropagationService;
        this.workerId = workerConnectionMessage.getWorkerId();
        port = workerConnectionMessage.getPort();
        address = workerConnectionMessage.getAddress();

        this.inputStream = inputStream;
        Socket socket = new Socket(address, port);
        this.outputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            Message message = null;

            do {
                int length = inputStream.readInt();
                byte[] bytes = inputStream.readNBytes(length);
                message = SerializationUtils.deserialize(bytes);

                messagePropagationService.propagateMessage(message, workerId);
            } while (message.getMessageType() != MessagesTypeEnum.WorkerDisconnectMessage || message.getMessageType() == MessagesTypeEnum.ShutDownMessage);
        } catch (Exception e){
            log.error("Fail messageHandler for worker id: {}", workerId, e);
        }
    }

    void send(Message message){
        try {
            byte[] bytes = SerializationUtils.serialize(message);
            int size = bytes.length;
            outputStream.writeInt(size);
            outputStream.flush();
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            log.error("Can not send message to workerId: {}", workerId, e);
        }
    }
}
