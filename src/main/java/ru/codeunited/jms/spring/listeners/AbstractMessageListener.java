package ru.codeunited.jms.spring.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import ru.codeunited.jms.spring.sender.MessageSender;

import javax.jms.*;

import static java.lang.String.format;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 09.10.15.
 */
public abstract class AbstractMessageListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(MessageListener.class);

    @Autowired
    protected JmsTemplate jmsTemplate;

    @Autowired
    private MessageSender sender;

    Logger getLogger() {
        return LOG;
    }

    void replyIfRequired(final Message request, final String messageBody) {
        Queue replyTo;
        try {
            replyTo = (Queue) request.getJMSReplyTo();
            sender.send(messageBody, replyTo.getQueueName());

        } catch (JMSException e) {
            getLogger().error("Can't send reply: {}", e.getMessage());
        }
    }

    public void logMessage(Message message) {
        try {
            getLogger().info(
                    format(
                            "{} got request {} Type: {}",
                            Thread.currentThread().getName(),
                            message.getJMSMessageID(),
                            message.getClass().getName()
                    )
            );
        } catch (JMSException e) {
            getLogger().error(e.getMessage());
        }
    }

}
