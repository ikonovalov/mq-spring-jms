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
public class ReceiveMessageACK {

    private static final Logger LOG = Logger.getLogger(ReceiveMessageACK.class.getName());

    public static void main(String[] args) throws JMSException {
        ConnectionFactory connectionFactory = getConnectionFactory();
        Connection connection = connectionFactory.createConnection("ikonovalov", "");
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        Queue queue = resolveQueue("JMS.SMPL.BUSN.REQ.ACK", session);
        MessageConsumer consumer = session.createConsumer(queue);

        connection.start();         // !DON'T FORGET!

        TextMessage message = (TextMessage) consumer.receive(1000L); // or receive(), or receiveNoWait
        if (message != null) {      // null - queue is empty.
            LOG.info("Message: " + message.getText());
            message.acknowledge();  // we use CLIENT_ACKNOWLEDGE
        }

        // release resources
        connection.stop();
        consumer.close();
        session.close();
        connection.close();

    }

}
