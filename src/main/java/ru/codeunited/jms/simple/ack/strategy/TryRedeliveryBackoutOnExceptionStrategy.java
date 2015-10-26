package ru.codeunited.jms.simple.ack.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.codeunited.jms.service.MessageLoggerService;
import ru.codeunited.jms.simple.AbstractBackoutExceptionHandligStrategy;
import ru.codeunited.jms.simple.ExceptionHandlingStrategy;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 26.10.15.
 */
public class TryRedeliveryBackoutOnExceptionStrategy extends AbstractBackoutExceptionHandligStrategy implements ExceptionHandlingStrategy {


    private static final Logger LOG = LoggerFactory.getLogger(TryRedeliveryBackoutOnExceptionStrategy.class);

    public TryRedeliveryBackoutOnExceptionStrategy(String backoutQueueName, MessageLoggerService messageLoggerService) {
        super(backoutQueueName, messageLoggerService);
    }

    @Override
    public void handle(Session session, Message message, Exception e) {
        try {
            String messageID = message.getJMSMessageID();

            // recover or backout
            if (message.getJMSRedelivered()) {
                moveToBackout(session, message, e);
                message.acknowledge();
                getMessageLoggerService().backout(getBackoutQueueName(), messageID, message);
            } else {
                session.recover();
                LOG.error("Session recovered");
            }

        } catch (JMSException e1) {
            LOG.error(e1.getMessage());
        }
    }
}
