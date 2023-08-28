package pl.edu.agh.hiputs.model.id;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class PatchIdSerializer extends Serializer<PatchId> {

  @Override
  public void write(Kryo kryo, Output output, PatchId object) {
    output.writeString(object.getValue());
  }

  @Override
  public PatchId read(Kryo kryo, Input input, Class<? extends PatchId> type) {
    String outerValue = input.readString();
    return new PatchId(outerValue);
  }
}
