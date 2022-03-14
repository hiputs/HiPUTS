package pl.edu.agh.communication;

import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.communication.model.messages.Message;
import pl.edu.agh.communication.model.messages.NeighbourConnectionMessage;
import pl.edu.agh.communication.utils.MessageConverter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;

/**
 * The class responsible for the connection with the neighbor.
 * The connection is one-way. You can only send a message to a neighbor.
 * Messages from all neighbors come on a proprietary socket.
 */
@Slf4j
public class NeighbourConnection {

    private OutputStream output;
    private final String id;

    public NeighbourConnection(NeighbourConnectionMessage message) {
        id = message.getId();
        try {
            Socket socket = new Socket(message.getAddress(), message.getPort());
            output = socket.getOutputStream();
        } catch (IOException e) {
            log.error("Error connection with neighbour ", e);
        }
    }

    public void send(Message message) throws IOException {
        if (Objects.isNull(output)) {
            log.info("Connection with worker " + id + " not exist");
            return;
        }

        byte[] encodedMsg = MessageConverter.toByteArray(message);

        if (Objects.isNull(encodedMsg)) {
            return;
        }

        output.write(encodedMsg);
    }
}
