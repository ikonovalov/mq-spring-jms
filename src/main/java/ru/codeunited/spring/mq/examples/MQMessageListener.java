package ru.codeunited.spring.mq.examples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * Created by Igor on 2014.07.31.
 */
public class MQMessageListener implements MessageListener {

    private static final Logger LOG = Logger.getLogger(MQMessageListener.class.getName());

    @Autowired
    private JmsTemplate jmsTemplate;

    @Override
    public void onMessage(Message message) {
        try {
            final String messageId = message.getJMSMessageID();
            LOG.info(
                    format(
                            "\n%s got message %s\nType: %s",
                            Thread.currentThread().getName(),
                            messageId,
                            message.getClass().getName()
                    )
            );

            if (isTextMessage(message)) {
                // Process text message
                final TextMessage textMessage = (TextMessage) message;
                final String payload = textMessage.getText();

                LOG.info(format("\nPayload:\n>%s...<", payload.substring(0,payload.length() > 50 ? 50 : payload.length())));

                // ready for reply
                final Destination replyTo = message.getJMSReplyTo();
                if (replyTo == null) { // quite eating messages without JMSReplyTo
                    LOG.warning(format("Message %s comes without JMSReplyTo", messageId));
                } else {
                    LOG.info("Reply to " + replyTo);
                    final AtomicReference<TextMessage> reposentRef = new AtomicReference<TextMessage>();
                    jmsTemplate.send(replyTo, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            final TextMessage response = session.createTextMessage();
                            response.setJMSCorrelationID(messageId);
                            response.setText("Ok");
                            reposentRef.set(response);
                            return response;
                        }
                    });
                    LOG.info(format("Sent reply for %s with MessageId=%s", messageId, reposentRef.get().getJMSMessageID()));
                }


            } else {
                final String wrongTypeMessage = "We don't handle messages other then TextMessage.";
                LOG.warning(wrongTypeMessage);
                throw new JMSException(wrongTypeMessage);
            }

        } catch (JMSException e) {
            LOG.severe(e.getMessage());
            // and put to DLQ or whatever...
        }
    }

    public boolean isTextMessage(Message message) {
        return message instanceof TextMessage;
    }
}
