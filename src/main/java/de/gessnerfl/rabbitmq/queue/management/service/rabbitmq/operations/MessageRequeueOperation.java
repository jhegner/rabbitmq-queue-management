package de.gessnerfl.rabbitmq.queue.management.service.rabbitmq.operations;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.ReturnListener;

@Service
public class MessageRequeueOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageRequeueOperation.class);
    private static final long MAX_WAIT_FOR_CONFIRM = 5000;
    private final MessageOperationExecutor messageOperationExecutor;

    @Autowired
    public MessageRequeueOperation(MessageOperationExecutor messageOperationExecutor) {
        this.messageOperationExecutor = messageOperationExecutor;
    }
    
    public void requeueFirstMessage(String queue, String checksum, String targetExchange, String targetRoutingKey){
        messageOperationExecutor.consumeMessageApplyFunctionAndAckknowlegeOnSuccess(queue, checksum, (channel,response) -> {
            StateKeepingReturnListener returnListener = new StateKeepingReturnListener();
            channel.addReturnListener(returnListener);
            channel.confirmSelect();
            
            channel.basicPublish(targetExchange, targetRoutingKey, true, response.getProps(), response.getBody());
            channel.waitForConfirmsOrDie(MAX_WAIT_FOR_CONFIRM);
            if(returnListener.isReceived()){
                throw new MessageOperationFailedException("Requeue failed, basic.return received");
            }
            
            channel.removeReturnListener(returnListener);
        });
    }
    
    private class StateKeepingReturnListener implements ReturnListener {
        private boolean received;

        @Override
        public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, BasicProperties properties, byte[] body) throws IOException {
            LOGGER.error("basic.return received: exchange={}, routingKey={}, replyCode={}, replyText={}", exchange, routingKey, replyCode, replyText);
            received = true;
        }
        
        public boolean isReceived(){
            return received;
        }
    }
}
