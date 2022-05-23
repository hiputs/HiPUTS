package pl.edu.agh.hiputs.communication;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.serializable.ConnectionDto;
import pl.edu.agh.hiputs.model.id.MapFragmentId;

/**
 * The class responsible for the connection with the neighbor.
 * The connection is one-way. You can only send a message to a neighbor.
 * Messages from all neighbors come on a dedicated socket.
 */
@Slf4j
public class Connection {

  private ObjectOutputStream output;
  private final MapFragmentId id;

  public Connection(ConnectionDto message) {
    id = new MapFragmentId(message.getId());
    try {
      Socket socket = new Socket(message.getAddress(), message.getPort());
      output = new ObjectOutputStream(socket.getOutputStream());
    } catch (IOException e) {
      log.error("Error connection with neighbour ", e);
    }
  }

  public void send(Message message) throws IOException {
    if (Objects.isNull(output)) {
      log.info("Connection with worker " + id + " not exist");
      return;
    }

    output.writeObject(message);
    output.flush();
  }
}
