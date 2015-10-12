package ru.codeunited.jms.simple.tx;

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
public class ListenMessageTX {

    private static final Logger LOG = Logger.getLogger(ListenMessageTX.class.getName());

    private static final BusinessService service = new BusinessNumberServiceImpl();

    public static void main(String[] args) throws JMSException, InterruptedException {
        ConnectionFactory connectionFactory = getConnectionFactory();
        Connection connection = connectionFactory.createConnection("ikonovalov", "");
        final Session session = connection.createSession(true, Session.CLIENT_ACKNOWLEDGE);

        Queue queue = resolveQueue("JMS.SMPL.BUSN.REQ.ACK", session);

        MessageConsumer consumer = session.createConsumer(queue);
        consumer.setMessageListener(new LogMessageListenerTX(session, service));

        connection.start();     // !DON'T FORGET!

        Thread.currentThread().join(10000L); // Consume only 10 second
        //session.commit();

        // release resources
        connection.stop();
        consumer.close();
        session.close();
        connection.close();

    }

    private static class LogMessageListenerTX implements MessageListener {

        private final Session session;

        private final BusinessService service;

        public LogMessageListenerTX(Session session, BusinessService service) {
            this.session = session;
            this.service = service;
        }

        @Override
        public void onMessage(Message message) {
            TextMessage textMessage = (TextMessage) message;
            try {
                String messageID = message.getJMSMessageID();
                LOG.info(String.format("Incoming message %s", messageID));

                BusinessResponse response = service.processRequest(new BusinessRequest(textMessage.getText()));

                session.commit(); // maybe AOP is better?
                LOG.info(String.format("Message [%s] handled", messageID));
            } catch (JMSException e) {
                try {
                    String messageID = message.getJMSMessageID();
                    LOG.severe(messageID + " got error: " + e.toString());
                    session.rollback();
                    LOG.warning(String.format("Message [%] rolled back", message.getJMSMessageID()));
                } catch (JMSException e1) {
                    LOG.severe(e1.getMessage());
                    throw new RuntimeException(e1);
                }
                throw new RuntimeException(e);
            }
        }
    }
}
