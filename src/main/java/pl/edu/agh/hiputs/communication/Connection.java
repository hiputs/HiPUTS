package pl.edu.agh.hiputs.communication;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
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

  private DataOutputStream output;
  private final MapFragmentId id;

  public Connection(ConnectionDto message) {
    id = new MapFragmentId(message.getId());
    for(int i = 0; i < 10; i++){
      try {
        Socket socket = new Socket(message.getAddress(), message.getPort());
        output = new DataOutputStream(socket.getOutputStream());
        return;
      } catch (IOException e) {
        log.warn("Error connection with neighbour {}", message.getId());
        try {
          Thread.sleep(1000 * (i+1));
        } catch (InterruptedException ex) {
          log.error("Thread error");
        }
      }
    }
    log.warn("Error connection with neighbour {}", message.getId());
  }

  public synchronized int send(Message message) throws IOException {
    if (Objects.isNull(output)) {
      log.info("Connection with worker {} not exist", id);
      return 0;
    }
    byte[] bytes = SerializationUtils.serialize(message);
    int size = bytes.length;

    output.writeInt(size);
    output.flush();
    output.write(bytes);
    output.flush();

    return size;
  }
}
