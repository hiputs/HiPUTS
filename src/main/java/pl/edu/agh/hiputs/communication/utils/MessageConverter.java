package pl.edu.agh.hiputs.communication.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import pl.edu.agh.hiputs.communication.model.messages.Message;

public class MessageConverter {

  public static byte[] toByteArray(Message message) throws IOException {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos)) {
      out.writeObject(message);
      out.flush();
      return bos.toByteArray();
    }
  }

  public static Message toMessage(byte[] encodedMessage) throws IOException, ClassNotFoundException {
    try (ByteArrayInputStream bis = new ByteArrayInputStream(encodedMessage);
        ObjectInputStream in = new ObjectInputStream(bis)) {
      return (Message) in.readObject();
    }
  }
}
