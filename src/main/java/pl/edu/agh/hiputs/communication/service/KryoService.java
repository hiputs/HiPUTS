package pl.edu.agh.hiputs.communication.service;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

@Getter
@Setter
public class KryoService {

  private final Kryo kryo;

  public KryoService() {
    this.kryo = new Kryo();
    MessagesTypeEnum.getMessagesClasses().forEach(kryo::register);
    kryo.register(List.of().getClass(), new JavaSerializer());
    kryo.register(java.util.ArrayList.class);
    kryo.register(java.util.LinkedList.class);
    kryo.register(java.util.HashMap.class);
    kryo.register(String.class);

  }
}
