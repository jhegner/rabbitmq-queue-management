package de.gessnerfl.rabbitmq.queue.management.controller;

import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Binding;
import de.gessnerfl.rabbitmq.queue.management.model.remoteapi.Exchange;
import de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.RabbitMqFacade;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MoveAllMessagesControllerTest {

    @Mock
    private RabbitMqFacade facade;
    @Mock
    private Logger logger;

    @InjectMocks
    private MoveAllMessagesController sut;

    @Test
    public void shouldReturnViewWithVhostAndQueueAsParameterOnGet(){
        final String vhost = "vhost";
        final String queue = "queue";
        final Model model = mock(Model.class);
        final Exchange exchange = mock(Exchange.class);
        final List<Exchange> exchangeList = Arrays.asList(exchange, exchange);

        when(facade.getExchanges(vhost)).thenReturn(exchangeList);

        String result = sut.getMoveAllMessagePage(vhost, queue, model);

        assertEquals(MoveAllMessagesController.VIEW_NAME, result);

        verify(facade).getExchanges(vhost);
        verify(model).addAttribute(Parameters.VHOST, vhost);
        verify(model).addAttribute(Parameters.QUEUE, queue);
        verify(model).addAttribute(Parameters.EXCHANGES, exchangeList);
        verifyNoInteractions(logger);
        verifyNoMoreInteractions(facade);
    }

    @Test
    public void shouldStayOnPageWhenTargetExchangeIsProvidedAndTargetRoutingKeyIsNotProvided(){
        final String routingKey1 = "routingKey1";
        final String routingKey2 = "routingKey2";
        final Binding binding1 = mock(Binding.class);
        final Binding binding2 = mock(Binding.class);
        final String vhost = "vhost";
        final String queue = "queue";
        final String targetExchange = "targetExchange";
        final Model model = mock(Model.class);
        final RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        when(binding1.getRoutingKey()).thenReturn(routingKey1);
        when(binding2.getRoutingKey()).thenReturn(routingKey2);
        when(facade.getExchangeSourceBindings(vhost, targetExchange)).thenReturn(Arrays.asList(binding1, binding2));

        String result = sut.moveAllMessages(vhost, queue, targetExchange, null, model, redirectAttributes);

        assertEquals(MoveAllMessagesController.VIEW_NAME, result);

        verify(facade).getExchangeSourceBindings(vhost, targetExchange);
        verify(model).addAttribute(Parameters.VHOST, vhost);
        verify(model).addAttribute(Parameters.QUEUE, queue);
        verify(model).addAttribute(Parameters.TARGET_EXCHANGE, targetExchange);
        verify(model).addAttribute(Parameters.ROUTING_KEYS, Arrays.asList(routingKey1, routingKey2));
        verifyNoInteractions(redirectAttributes, logger);
        verifyNoMoreInteractions(facade, model);
    }

    @Test
    public void shouldMoveAllMessagesAndRedirectToMessagesPageWhenTargetExchangeAndRoutingKeyAreProvidedAndMoveWasSuccessful(){
        final String vhost = "vhost";
        final String queue = "queue";
        final String targetExchange = "targetExchange";
        final String targetRoutingKey = "targetRoutingKey";
        final Model model = mock(Model.class);
        final RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        String result = sut.moveAllMessages(vhost, queue, targetExchange, targetRoutingKey, model, redirectAttributes);

        assertEquals(Pages.MESSAGES.redirectTo(), result);

        verify(facade).moveAllMessagesInQueue(vhost, queue, targetExchange, targetRoutingKey);
        verify(redirectAttributes).addAttribute(Parameters.VHOST, vhost);
        verify(redirectAttributes).addAttribute(Parameters.QUEUE, queue);
        verifyNoInteractions(model, logger);
        verifyNoMoreInteractions(facade, redirectAttributes);
    }

    @Test
    public void shouldTryToMoveAllMessagesAndStayOnMoveAllMessagePageWhenTargetExchangeAndRoutingKeyAreProvidedAndMoveFailed(){
        final String errorMessage = "error";
        final RuntimeException expectedException = new RuntimeException(errorMessage);
        final String vhost = "vhost";
        final String queue = "queue";
        final String targetExchange = "targetExchange";
        final String targetRoutingKey = "targetRoutingKey";
        final Model model = mock(Model.class);
        final RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        doThrow(expectedException).when(facade).moveAllMessagesInQueue(vhost, queue, targetExchange, targetRoutingKey);

        String result = sut.moveAllMessages(vhost, queue, targetExchange, targetRoutingKey, model, redirectAttributes);

        assertEquals(MoveAllMessagesController.VIEW_NAME, result);

        verify(facade).moveAllMessagesInQueue(vhost, queue, targetExchange, targetRoutingKey);
        verify(logger).error(anyString(), eq(queue), eq(vhost), eq(targetExchange), eq(targetRoutingKey),  eq(expectedException));
        verify(model).addAttribute(Parameters.VHOST, vhost);
        verify(model).addAttribute(Parameters.QUEUE, queue);
        verify(model).addAttribute(Parameters.TARGET_EXCHANGE, targetExchange);
        verify(model).addAttribute(Parameters.TARGET_ROUTING_KEY, targetRoutingKey);
        verify(model).addAttribute(Parameters.ERROR_MESSAGE, errorMessage);
        verifyNoInteractions(redirectAttributes);
        verifyNoMoreInteractions(facade, model, logger);
    }
}