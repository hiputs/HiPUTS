package pl.edu.agh.hiputs.communication.service.server;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.IOException;
import java.net.Socket;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.WorkerConnectionMessage;
import pl.edu.agh.hiputs.communication.service.KryoService;

@Slf4j
public class WorkerConnection implements Runnable {

    @Getter
    private final String workerId;
    @Getter
    private final int port;
    @Getter
    private final String address;
    private final MessagePropagationService messagePropagationService;
    private final Output outputStream;
    private final Input inputStream;
    private final KryoService kryoIn;
    private final KryoService kryoOut;

    public WorkerConnection(Input inputStream, MessagePropagationService messagePropagationService,
        WorkerConnectionMessage workerConnectionMessage) throws IOException {
        this.kryoIn = new KryoService();
        this.kryoOut = new KryoService();
        this.messagePropagationService = messagePropagationService;
        this.workerId = workerConnectionMessage.getWorkerId();
        port = workerConnectionMessage.getPort();
        address = workerConnectionMessage.getAddress();

        this.inputStream = inputStream;
        Socket socket = new Socket(address, port);
        this.outputStream = new Output(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            Message message = null;

            do {
                // int length = inputStream.readInt();
                // byte[] bytes = inputStream.readNBytes(length);
                // message = SerializationUtils.deserialize(bytes);
                message = (Message) kryoIn.getKryo().readClassAndObject(inputStream);

                messagePropagationService.propagateMessage(message, workerId);
            } while (message.getMessageType() != MessagesTypeEnum.WorkerDisconnectMessage || message.getMessageType() == MessagesTypeEnum.ShutDownMessage);
        } catch (Exception e){
            log.error("Fail messageHandler for worker id: {}", workerId, e);
        }
    }

    void send(Message message) {
        // byte[] bytes = SerializationUtils.serialize(message);
        // int size = bytes.length;
        // outputStream.writeInt(size);
        // outputStream.flush();
        // outputStream.write(bytes);
        // outputStream.flush();
        log.debug("Sending msg {}", message.getMessageType());
        kryoOut.getKryo().writeClassAndObject(outputStream, message);
        log.debug("after kyro {}", outputStream.getBuffer().length);
        outputStream.flush();
        log.debug("Sent msg {}", message.getMessageType());
    }
}
