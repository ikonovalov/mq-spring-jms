package ru.codeunited.jms.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.codeunited.jms.service.MessageLoggerService;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import static ru.codeunited.jms.simple.JmsHelper.copyMessage;
import static ru.codeunited.jms.simple.JmsHelper.resolveQueue;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 26.10.15.
 */
public abstract class AbstractBackoutExceptionHandligStrategy extends AbstractExceptionHandligStrategy {

    protected static final Logger LOG = LoggerFactory.getLogger(AbstractBackoutExceptionHandligStrategy.class);

    private final String backoutQueueName;

    protected AbstractBackoutExceptionHandligStrategy(String backoutQueueName, MessageLoggerService messageLoggerService) {
        super(messageLoggerService);
        this.backoutQueueName = backoutQueueName;
    }

    public String getBackoutQueueName() {
        return backoutQueueName;
    }

    protected void moveToBackout(Session session, Message message, Exception exception) {
        try {
            if (getBackoutQueueName() != null) {
                MessageProducer producer = session.createProducer(resolveQueue(getBackoutQueueName(), session));
                Message messageCopy = copyMessage(session, message);
                messageCopy.setStringProperty("Error", exception.getMessage());

                producer.send(messageCopy);
                producer.close();
            }
        } catch (JMSException e) {
            LOG.error(e.getMessage());
        }
    }
}
