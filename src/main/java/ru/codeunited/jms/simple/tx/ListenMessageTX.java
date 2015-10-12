package ru.codeunited.jms.simple.tx;

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

    public static void main(String[] args) throws JMSException, InterruptedException {
        ConnectionFactory connectionFactory = getConnectionFactory();
        Connection connection = connectionFactory.createConnection("ikonovalov", "");
        final Session session = connection.createSession(true, Session.CLIENT_ACKNOWLEDGE);

        Queue queue = resolveQueue("JMS.SMPL.BUSN.REQ.ACK", session);

        // single thread scope
        MessageConsumer consumer = session.createConsumer(queue);
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                TextMessage textMessage = (TextMessage) message;

                try {
                    LOG.info(Thread.currentThread().getName() + " - Message listener: " + textMessage.getText());
                    session.commit(); // maybe AOP is better?
                } catch (JMSException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        // single thread scope end


        connection.start();     // !DON'T FORGET!

        Thread.currentThread().join(1000L); // Consume only 1 second
        //session.commit();

        // release resources
        connection.stop();
        consumer.close();
        session.close();
        connection.close();

    }
}
