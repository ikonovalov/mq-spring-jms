package ru.codeunited.jms.simple.ack.strategy;

import ru.codeunited.jms.service.MessageLoggerService;
import ru.codeunited.jms.simple.AbstractBackoutExceptionHandligStrategy;
import ru.codeunited.jms.simple.AbstractExceptionHandligStrategy;
import ru.codeunited.jms.simple.ExceptionHandlingStrategy;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 26.10.15.
 */
public class BackoutOnExceptionStrategy extends AbstractBackoutExceptionHandligStrategy implements ExceptionHandlingStrategy {

    public BackoutOnExceptionStrategy(String backoutQueueName, MessageLoggerService messageLoggerService) {
        super(backoutQueueName, messageLoggerService);
    }

    @Override
    public void handle(Session session, Message message, Exception e) {
        try {
            String messageID = message.getJMSMessageID();
            moveToBackout(session, message, e);
            message.acknowledge();
            getMessageLoggerService().backout(getBackoutQueueName(), messageID, message);

        } catch (JMSException innerE) {
            LOG.error(innerE.getMessage());
        }
    }
}
