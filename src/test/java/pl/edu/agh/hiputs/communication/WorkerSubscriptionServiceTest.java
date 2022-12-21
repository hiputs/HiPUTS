package pl.edu.agh.hiputs.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.NeighbourConnectionMessage;
import pl.edu.agh.hiputs.communication.model.messages.RunSimulationMessage;
import pl.edu.agh.hiputs.communication.service.worker.MessageReceiverService;
import pl.edu.agh.hiputs.communication.service.worker.WorkerSubscriptionService;

@SpringBootTest
@Disabled("TODO remove visualization during test")
public class WorkerSubscriptionServiceTest {

  @Autowired
  private WorkerSubscriptionService subscriptionService;

  @Autowired
  private MessageReceiverService messageReceiverService;

  @Test
  void registerSubscriberAndCheckPropagateMessage() {
    // given
    PrimitiveSubscriber primitiveSubscriber = new PrimitiveSubscriber();
    Message message = NeighbourConnectionMessage.builder().build();

    // when
    subscriptionService.subscribe(primitiveSubscriber, MessagesTypeEnum.WorkerConnectionMessage);
    messageReceiverService.propagateMessage(message);

    //then
    assertEquals(1, primitiveSubscriber.receivedMessageCounter);
  }

  @Test
  void registerSubscriberWithMultiMessagesAndCheckPropagateMessage() {
    // given
    PrimitiveSubscriber primitiveSubscriber = new PrimitiveSubscriber();
    Message message1 = NeighbourConnectionMessage.builder().build();
    Message message2 = new RunSimulationMessage();

    // when
    subscriptionService.subscribe(primitiveSubscriber, MessagesTypeEnum.WorkerConnectionMessage);
    subscriptionService.subscribe(primitiveSubscriber, MessagesTypeEnum.RunSimulationMessage);

    messageReceiverService.propagateMessage(message1);
    messageReceiverService.propagateMessage(message2);
    messageReceiverService.propagateMessage(message1);

    //then
    assertEquals(3, primitiveSubscriber.receivedMessageCounter);
    assertEquals(2, primitiveSubscriber.type1);
    assertEquals(1, primitiveSubscriber.type2);
  }

  private static class PrimitiveSubscriber implements Subscriber {

    public int receivedMessageCounter = 0;
    public int type1 = 0;
    public int type2 = 0;

    @Override
    public void notify(Message message) {
      receivedMessageCounter++;

      if (message.getMessageType() == MessagesTypeEnum.WorkerConnectionMessage) {
        type1++;
      } else {
        type2++;
      }
    }
  }
}
