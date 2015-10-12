package ru.codeunited.jms.simple.ack;

import ru.codeunited.jms.service.BusinessNumberServiceImpl;
import ru.codeunited.jms.service.BusinessRequest;
import ru.codeunited.jms.service.BusinessResponse;
import ru.codeunited.jms.service.BusinessService;

import javax.jms.*;

import java.util.logging.Logger;

import static ru.codeunited.jms.simple.JmsHelper.getConnectionFactory;
import static ru.codeunited.jms.simple.JmsHelper.resolveQueue;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 12.10.15.
 */
public class ListenMessageACK {

    private static final Logger LOG = Logger.getLogger(ListenMessageACK.class.getName());

    private static final BusinessService service = new BusinessNumberServiceImpl();

    public static void main(String[] args) throws JMSException, InterruptedException {
        ConnectionFactory connectionFactory = getConnectionFactory();
        Connection connection = connectionFactory.createConnection("ikonovalov", "");
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        Queue queue = resolveQueue("JMS.SMPL.BUSN.REQ.ACK", session);
        MessageConsumer consumer = session.createConsumer(queue);

        consumer.setMessageListener(new LogMessageListenerACK(service, session));

        connection.start();     // !DON'T FORGET!

        Thread.currentThread().join(1000L);

        // release resources
        connection.stop();
        consumer.close();
        session.close();
        connection.close();

    }

    private static class LogMessageListenerACK implements MessageListener {

        private final BusinessService service;

        private final Session session;

        private LogMessageListenerACK(BusinessService service, Session session) {
            this.service = service;
            this.session = session;
        }

        @Override
        public void onMessage(Message message) {
            TextMessage textMessage = (TextMessage) message;

            try {
                String messageID = message.getJMSMessageID();
                LOG.info(String.format("Incoming message %s", messageID));

                BusinessResponse response = service.processRequest(new BusinessRequest(textMessage.getText()));

                message.acknowledge();
                LOG.info(String.format("Message [%s] handled", messageID));
            } catch (JMSException e) {
                try {
                    String messageID = message.getJMSMessageID();
                    LOG.severe(messageID + " got error: " + e.toString());
                    session.recover();
                    LOG.warning("Session recovered");
                } catch (JMSException e1) {
                    LOG.severe(e1.getMessage());
                    throw new RuntimeException(e1);
                }
                throw new RuntimeException(e);
            }
        }
    }
}
