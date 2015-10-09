package ru.codeunited.spring.mq.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 09.10.15.
 */
public abstract class AbstractMessageListener implements MessageListener {

    private static final Logger LOG = Logger.getLogger(MQMessageListener.class.getName());

    @Autowired
    protected JmsTemplate jmsTemplate;

    public Logger getLogger() {
        return LOG;
    }

    protected void replyIfRequired(final Message request, final String messageBody) throws JMSException {
        final Destination replyTo = request.getJMSReplyTo();
        if (replyTo != null) { // quite eating messages without JMSReplyTo
            final AtomicReference<TextMessage> responseRef = new AtomicReference<>();
            jmsTemplate.send(replyTo, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    final TextMessage response = session.createTextMessage();
                    response.setJMSCorrelationID(request.getJMSMessageID());
                    response.setText("Ok");
                    responseRef.set(response);
                    return response;
                }
            });
            getLogger().info(format("Sent reply for %s with MessageId=%s", request.getJMSMessageID(), responseRef.get().getJMSMessageID()));
        } else {
            getLogger().info(format("Reply not required for %s", request.getJMSMessageID()));
        }
    }

    public boolean isTextMessage(Message message) {
        return message instanceof TextMessage;
    }

    public void logMessage(Message message) {
        try {
            getLogger().info(
                    format(
                            "\n%s got request %s\nType: %s",
                            Thread.currentThread().getName(),
                            message.getJMSMessageID(),
                            message.getClass().getName()
                    )
            );
        } catch (JMSException e) {
            getLogger().severe(e.getMessage());
        }
    }

}
