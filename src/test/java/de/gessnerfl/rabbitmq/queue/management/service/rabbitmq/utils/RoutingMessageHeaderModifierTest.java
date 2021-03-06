package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.utils;

import com.rabbitmq.client.AMQP;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations.OperationId;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

public class RoutingMessageHeaderModifierTest {

    private static final String TEST_COUNTER = "x-test-counter";
    private static final OperationId OPERATION_ID = new OperationId();
    private RoutingMessageHeaderModifier sut;

    @Before
    public void init(){
        sut = new RoutingMessageHeaderModifier();
    }

    @Test
    public void shouldSetOperationIdAndIncrementCounterHeaderWhenNoHeadersAreDefined(){
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().contentType("text/plain").build();

        AMQP.BasicProperties result = sut.modifyHeaders(properties, OPERATION_ID, TEST_COUNTER);

        assertNotEquals(properties, result); //Should be a copy
        assertEquals(1, result.getHeaders().get(TEST_COUNTER));
        assertEquals(OPERATION_ID.getValue(), result.getHeaders().get(OperationId.HEADER_NAME));
    }

    @Test
    public void shouldSetOperationIdAndIncrementCounterHeaderWhenHeadersIsDefinedAndCountHeaderIsNotSet(){
        Map<String,Object> headers = new HashMap<>();
        headers.put("foo", "bar");
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().contentType("text/plain").headers(headers).build();

        AMQP.BasicProperties result = sut.modifyHeaders(properties, OPERATION_ID, TEST_COUNTER);

        assertNotEquals(properties, result); //Should be a copy
        assertEquals(1, result.getHeaders().get(TEST_COUNTER));
        assertEquals(OPERATION_ID.getValue(), result.getHeaders().get(OperationId.HEADER_NAME));
    }

    @Test
    public void shouldSetOperationIdAndIncrementCounterHeaderWhenHeadersIsDefinedAndCountHeaderIsSet(){
        int initialValue = 2;
        Map<String,Object> headers = new HashMap<>();
        headers.put(TEST_COUNTER, initialValue);
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().contentType("text/plain").headers(headers).build();

        AMQP.BasicProperties result = sut.modifyHeaders(properties, OPERATION_ID, TEST_COUNTER);

        assertNotEquals(properties, result); //Should be a copy
        assertEquals(initialValue+1, result.getHeaders().get(TEST_COUNTER));
        assertEquals(OPERATION_ID.getValue(), result.getHeaders().get(OperationId.HEADER_NAME));
    }

    @Test
    public void shouldSetOperationIdAndIncrementCounterHeaderWhenHeadersIsDefinedAndOperationIdHeaderIsSet(){
        String initialValue = "initialOperationId";
        Map<String,Object> headers = new HashMap<>();
        headers.put(OperationId.HEADER_NAME, initialValue);
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().contentType("text/plain").headers(headers).build();

        AMQP.BasicProperties result = sut.modifyHeaders(properties, OPERATION_ID, TEST_COUNTER);

        assertNotEquals(properties, result); //Should be a copy
        assertEquals(1, result.getHeaders().get(TEST_COUNTER));
        assertEquals(OPERATION_ID.getValue(), result.getHeaders().get(OperationId.HEADER_NAME));
    }

}