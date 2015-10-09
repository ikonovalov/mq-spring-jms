package ru.codeunited.spring.mq.examples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import ru.codeunited.spring.mq.service.BusinessRequest;
import ru.codeunited.spring.mq.service.BusinessResponse;
import ru.codeunited.spring.mq.service.BusinessService;

import javax.jms.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * Transport front-side.
 * Created by Igor on 2014.07.31.
 */
public class MQMessageListener implements MessageListener {

    private static final Logger LOG = Logger.getLogger(MQMessageListener.class.getName());

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private BusinessService businessService;

    @Override
    public void onMessage(Message message) {
        try {
            logMessage(message);

            if (isTextMessage(message)) {
                // Process text message
                String payload = ((TextMessage) message).getText();

                BusinessResponse response = businessService.processRequest(new BusinessRequest(payload));

                replyIfRequired(message, response.getPayload());

            } else {
                replyIfRequired(message, "We don't handle messages other then TextMessage.");
            }

        } catch (JMSException e) {
            LOG.severe(e.getMessage());
        }
    }

    private void replyIfRequired(final Message message, final String messageBody) throws JMSException {
        final Destination replyTo = message.getJMSReplyTo();
        if (replyTo != null) { // quite eating messages without JMSReplyTo
            final AtomicReference<TextMessage> responseRef = new AtomicReference<TextMessage>();
            jmsTemplate.send(replyTo, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    final TextMessage response = session.createTextMessage();
                    response.setJMSCorrelationID(message.getJMSMessageID());
                    response.setText("Ok");
                    responseRef.set(response);
                    return response;
                }
            });
            LOG.info(format("Sent reply for %s with MessageId=%s", message.getJMSMessageID(), responseRef.get().getJMSMessageID()));
        } else {
            LOG.info(format("Reply not required for %s", message.getJMSMessageID()));
        }
    }

    public boolean isTextMessage(Message message) {
        return message instanceof TextMessage;
    }

    public void logMessage(Message message) {
        try {
            LOG.info(
                    format(
                            "\n%s got message %s\nType: %s",
                            Thread.currentThread().getName(),
                            message.getJMSMessageID(),
                            message.getClass().getName()
                    )
            );
        } catch (JMSException e) {
           LOG.severe(e.getMessage());
        }
    }
}
