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
public class ReceiveMessageTX {

    private static final Logger LOG = Logger.getLogger(ReceiveMessageTX.class.getName());

    public static void main(String[] args) throws JMSException {
        ConnectionFactory connectionFactory = getConnectionFactory();
        Connection connection = connectionFactory.createConnection("ikonovalov", "");

        // WORK UNIT START
        Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
        Queue queue = resolveQueue("JMS.SMPL.BUSN.REQ.ACK", session);
        MessageConsumer consumer = session.createConsumer(queue);

        connection.start();

        for (int z = 0; z < 10; z++) { // consume 10 messages in transaction
            TextMessage message = (TextMessage) consumer.receive(1000L);
            if (message != null) { // null - queue is empty.
                LOG.info("Message: " + message.getText());
            }
        }
        session.commit(); // We use transacted session
        // WORK UNIT END

        // release resources
        connection.stop();
        consumer.close();
        session.close();
        connection.close();

    }

}
