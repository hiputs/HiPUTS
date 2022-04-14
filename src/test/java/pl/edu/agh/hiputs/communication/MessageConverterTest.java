package pl.edu.agh.hiputs.communication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.NeighbourConnectionMessage;
import pl.edu.agh.hiputs.communication.utils.MessageConverter;

public class MessageConverterTest {

  @Disabled("TODO why checking bytes?")
  @Test
  void messageToByte() throws IOException {
    // given
    Message message =
        NeighbourConnectionMessage.builder().address("123.456.789.000").port(1234).id("ASD:TREYT").build();
    // when
    byte[] encodedBytes = MessageConverter.toByteArray(message);
    // then
    assertEquals(169, encodedBytes.length);
  }

  @Test
  void fullCycle() throws IOException, ClassNotFoundException {
    // given
    Message message =
        NeighbourConnectionMessage.builder().address("123.456.789.000").port(1234).id("ASD:TREYT").build();
    // when
    byte[] encodedBytes = MessageConverter.toByteArray(message);
    Message resultMessage = MessageConverter.toMessage(encodedBytes);

    // then
    assertThat(resultMessage).usingRecursiveComparison().isEqualTo(message);
  }
}
