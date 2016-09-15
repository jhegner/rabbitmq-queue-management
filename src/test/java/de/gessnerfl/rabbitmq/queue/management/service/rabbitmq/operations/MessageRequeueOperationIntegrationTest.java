package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.gessnerfl.rabbitmq.queue.management.model.Message;

public class MessageRequeueOperationIntegrationTest extends AbstractOperationIntegrationTest {
    protected final static String TARGET_QUEUE_NAME = "test.requeue.target";
    @Autowired
    private QueueListOperation queueListOperation;

    @Autowired
    public MessageRequeueOperation sut;

    @Override
    protected List<String> getQueueNames() {
        return Arrays.asList(QUEUE_NAME, TARGET_QUEUE_NAME);
    }

    @Test
    public void shouldRequeueMessage() throws Exception {
        publishMessages(1);

        List<Message> sourceFirstFetch = queueListOperation.getMessagesFromQueue(QUEUE_NAME, 5);
        List<Message> targetFirstFetch = queueListOperation.getMessagesFromQueue(TARGET_QUEUE_NAME, 5);

        assertThat(sourceFirstFetch, hasSize(1));
        assertThat(targetFirstFetch, empty());

        sut.requeueFirstMessage(QUEUE_NAME, sourceFirstFetch.get(0).getChecksum(), EXCHANGE_NAME, TARGET_QUEUE_NAME);

        List<Message> sourceSecondFetch = queueListOperation.getMessagesFromQueue(QUEUE_NAME, 5);
        List<Message> targetSecondFetch = queueListOperation.getMessagesFromQueue(TARGET_QUEUE_NAME, 5);

        assertThat(sourceSecondFetch, empty());
        assertThat(targetSecondFetch, hasSize(1));
    }

    @Test
    public void shouldFailToRequeueMessageWhenExchangeIsNotValid() throws Exception {
        publishMessages(1);

        List<Message> sourceFirstFetch = queueListOperation.getMessagesFromQueue(QUEUE_NAME, 5);
        List<Message> targetFirstFetch = queueListOperation.getMessagesFromQueue(TARGET_QUEUE_NAME, 5);

        assertThat(sourceFirstFetch, hasSize(1));
        assertThat(targetFirstFetch, empty());

        try {
            sut.requeueFirstMessage(QUEUE_NAME, sourceFirstFetch.get(0).getChecksum(), "invalidExchangeName", TARGET_QUEUE_NAME);
            fail();
        } catch (MessageOperationFailedException e) {
        }

        List<Message> sourceSecondFetch = queueListOperation.getMessagesFromQueue(QUEUE_NAME, 5);
        List<Message> targetSecondFetch = queueListOperation.getMessagesFromQueue(TARGET_QUEUE_NAME, 5);

        assertThat(sourceSecondFetch, hasSize(1));
        assertThat(targetSecondFetch, empty());
    }

    @Test
    public void shouldFailToRequeueMessageWhenNoQueueIsBoundToTheGivenRoutingKey() throws Exception {
        publishMessages(1);

        List<Message> sourceFirstFetch = queueListOperation.getMessagesFromQueue(QUEUE_NAME, 5);
        List<Message> targetFirstFetch = queueListOperation.getMessagesFromQueue(TARGET_QUEUE_NAME, 5);

        assertThat(sourceFirstFetch, hasSize(1));
        assertThat(targetFirstFetch, empty());

        try {
            sut.requeueFirstMessage(QUEUE_NAME, sourceFirstFetch.get(0).getChecksum(), EXCHANGE_NAME, "invalidRoutingKey");
            fail();
        } catch (MessageOperationFailedException e) {
        }

        List<Message> sourceSecondFetch = queueListOperation.getMessagesFromQueue(QUEUE_NAME, 5);
        List<Message> targetSecondFetch = queueListOperation.getMessagesFromQueue(TARGET_QUEUE_NAME, 5);

        assertThat(sourceSecondFetch, hasSize(1));
        assertThat(targetSecondFetch, empty());
    }

}
