package pl.edu.agh.hiputs.communication.model.serializable;

import java.io.Serializable;

public interface CustomSerializable<E> extends Serializable {

    E toRealObject();

}
