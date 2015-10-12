package ru.codeunited.jms.simple.ack;

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

    public static void main(String[] args) throws JMSException, InterruptedException {
        ConnectionFactory connectionFactory = getConnectionFactory();
        Connection connection = connectionFactory.createConnection("ikonovalov", "");
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        Queue queue = resolveQueue("JMS.SMPL.BUSN.REQ.ACK", session);
        MessageConsumer consumer = session.createConsumer(queue);

        consumer.setMessageListener(new LogMessageListenerACK());

        connection.start();     // !DON'T FORGET!

        Thread.currentThread().join(1000L);

        // release resources
        connection.stop();
        consumer.close();
        session.close();
        connection.close();

    }

    private static class LogMessageListenerACK implements MessageListener {
        @Override
        public void onMessage(Message message) {
            TextMessage textMessage = (TextMessage) message;

            try {
                LOG.info("Message listener: " + textMessage.getText());

                message.acknowledge();
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
