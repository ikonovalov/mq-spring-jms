package ru.codeunited.jms.simple;

import com.ibm.mq.jms.MQQueueConnectionFactory;

import javax.jms.*;
import java.util.Date;
import java.util.logging.Logger;

import static ru.codeunited.jms.simple.JmsHelper.getConnectionFactory;
import static ru.codeunited.jms.simple.JmsHelper.resolveQueue;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 12.10.15.
 */
public class SendMessageTX {

    private static final Logger LOG = Logger.getLogger(SendMessageTX.class.getName());

    public static void main(String[] args) throws JMSException {
        MQQueueConnectionFactory connectionFactory = getConnectionFactory();

        Connection connection = null;
        try {
            connection = connectionFactory.createConnection("ikonovalov", "");

            // single thread scope!
            Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            Queue queue = resolveQueue("JMS.SMPL.BUSN.REQ.ACK", session);
            MessageProducer producer = session.createProducer(queue);

            String messageBody = "Now " + new Date();
            for (int z = 0; z < 10; z++) { // send 10 messages out-of-transaction
                Message mes = sendMessage(session, producer, messageBody + "#" + z);
                LOG.info(mes.getJMSMessageID());
            }

            session.commit(); // fix 10 messages

            // release resources
            producer.close();
            session.close();

            // single thread scope end

        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private static Message sendMessage(Session session, MessageProducer producer, String messageBody) throws JMSException {
        TextMessage message = session.createTextMessage();
        message.setText(messageBody);
        producer.send(message);
        return message;
    }


}
