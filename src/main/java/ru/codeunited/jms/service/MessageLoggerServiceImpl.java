package ru.codeunited.jms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.Date;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 14.10.15.
 */
@Service
public class MessageLoggerServiceImpl implements MessageLoggerService {

    private static final Logger logger = LoggerFactory.getLogger(MessageLoggerServiceImpl.class);

    @Override
    public void incoming(Message message) {
        try {
            logger.info("Incoming {} message. ID={}", message.getClass().getSimpleName(), message.getJMSMessageID());
        } catch (JMSException e) {
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void incoming(TextMessage message) {
        incoming((Message) message);

        try {
            logger.debug(message.getText());
        } catch (JMSException e) {
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void error(Message message, Exception e) {
        try {
            logger.error("Message ID={} cause error: {}. Message time stamp: {}. Redelivery={}", message.getJMSMessageID(), e.getMessage(), new Date(message.getJMSTimestamp()), getRedeliveryCount(message));
        } catch (JMSException ejms) {
            logger.warn(ejms.getMessage());
        }
    }

    @Override
    public void handled(Message message) {
        try {
            logger.info("Message ID={} handled", message.getJMSMessageID());
        } catch (JMSException e) {
            logger.warn(e.getMessage());
        }
    }

    Integer getRedeliveryCount(Message message) {
        try {
            return message.getIntProperty("JMSXDeliveryCount");
        } catch (JMSException e) {
            return null;
        }
    }

    @Override
    public void rollback(Message message) {
        try {

            logger.info("Message {} is rolled back. Redelivery count {}", message.getJMSMessageID(), getRedeliveryCount(message));
        } catch (JMSException e) {
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void backout(String backoutQueue, String originalMessageId, Message message) {
        try {
            logger.warn("Message ID={} going to backout to [{}] with {}", originalMessageId, backoutQueue, message.getJMSMessageID());
        } catch (JMSException e) {
            logger.warn(e.getMessage());
        }
    }

}
