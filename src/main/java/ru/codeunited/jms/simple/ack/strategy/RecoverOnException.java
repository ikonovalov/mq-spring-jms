package ru.codeunited.jms.simple.ack.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.codeunited.jms.simple.ExceptionHandlingStrategy;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 26.10.15.
 */
public class RecoverOnException implements ExceptionHandlingStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(RecoverOnException.class);

    @Override
    public void handle(Session session, Message message, Exception e) {
        try {
            session.recover();
            LOG.warn("Session recovered");
        } catch (JMSException e1) {
            LOG.error(e1.getMessage());
        }

    }
}
