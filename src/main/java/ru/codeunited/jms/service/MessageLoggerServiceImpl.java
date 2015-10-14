package ru.codeunited.jms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 14.10.15.
 */
@Service
public class MessageLoggerServiceImpl implements MessageLoggerService {

    private Logger logger = LoggerFactory.getLogger(MessageLoggerServiceImpl.class);

    @Override
    public void incoming(Message message) {
        try {
            logger.info("Incoming %s message. ID=%s", message.getClass().getSimpleName(), message.getJMSMessageID());
        } catch (JMSException e) {
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void error(Message message, Exception e) {
        try {
            logger.error("Message ID=%s cause error: %s", message.getJMSMessageID(), e.getMessage());
        } catch (JMSException ejms) {
            logger.warn(ejms.getMessage());
        }
    }

    @Override
    public void handled(Message message) {
        try {
            logger.info("Message ID=%s handled", message.getJMSMessageID());
        } catch (JMSException e) {
            logger.warn(e.getMessage());
        }
    }

}
