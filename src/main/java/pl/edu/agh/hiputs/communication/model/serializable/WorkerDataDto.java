package pl.edu.agh.hiputs.communication.model.serializable;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkerDataDto implements Serializable {
    List<String> patchIds;
    ConnectionDto connectionData;
}
