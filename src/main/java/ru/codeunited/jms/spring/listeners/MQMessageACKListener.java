package ru.codeunited.jms.spring.listeners;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.logging.Logger;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 09.10.15.
 */
@Component
public class MQMessageACKListener extends MQMessageTXListener {

    private String backoutQueue;

    public String getBackoutQueue() {
        return backoutQueue;
    }

    @Value("${queue.request.backout}")
    public void setBackoutQueue(String backoutQueue) {
        this.backoutQueue = backoutQueue;
    }

    @Override
    public void onMessage(Message message) {
        boolean redelivered = false;
        try {
            redelivered = message.getJMSRedelivered();
            if (redelivered) {
                getLogger().error("Message is redelivered! [{}]", message.getJMSMessageID());
            }
            super.onMessage(message);
        } catch (Exception e) {
            getLogger().error(e.getMessage());
            if (redelivered) {                  // redelivery failed? move to trash
                moveToBackout(message);
            } else {                            // give it only one chance
                throw new RuntimeException(e);
            }
        }
    }

    public void moveToBackout(final Message message) {
        try {
            if (getBackoutQueue() != null) {
                final String originalMessageID = message.getJMSMessageID();
                jmsTemplate.send(getBackoutQueue(), new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        message.setJMSCorrelationID(originalMessageID);
                        return message;
                    }
                });
                getLogger().info("Message is backed out [{}]", originalMessageID);

            }
        } catch (JMSException e) {
            getLogger().error(e.getMessage());
        }
    }
}
