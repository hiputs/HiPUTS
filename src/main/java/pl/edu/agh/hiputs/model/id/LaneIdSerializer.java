package pl.edu.agh.hiputs.model.id;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class LaneIdSerializer extends Serializer<LaneId> {

  @Override
  public void write(Kryo kryo, Output output, LaneId object) {
    output.writeString(object.getValue());
  }

  @Override
  public LaneId read(Kryo kryo, Input input, Class<? extends LaneId> type) {
    String outerValue = input.readString();
    return new LaneId(outerValue);
  }
}
