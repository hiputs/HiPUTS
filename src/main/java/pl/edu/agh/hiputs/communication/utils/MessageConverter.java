package pl.edu.agh.hiputs.communication.utils;

import pl.edu.agh.hiputs.communication.model.messages.Message;

import java.io.*;

public class MessageConverter {

    public static byte[] toByteArray(Message message) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] encodedMsg;
        try {
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(message);
            out.flush();
            encodedMsg = bos.toByteArray();
        } finally {
            bos.close();
        }

        return encodedMsg;
    }

    public static Message toMessage(byte[] encodedMessage) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bos = new ByteArrayInputStream(encodedMessage);
        Message message;

        try {
            ObjectInputStream in = new ObjectInputStream(bos);
            message = (Message) in.readObject();
        } finally {
            bos.close();
        }

        return message;
    }
}
