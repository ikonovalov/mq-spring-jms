package ru.codeunited.jms.simple;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

import static ru.codeunited.jms.simple.JmsHelper.connect;
import static ru.codeunited.jms.simple.JmsHelper.getConnectionFactory;
import static ru.codeunited.jms.simple.JmsHelper.resolveQueue;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 12.10.15.
 */
public class SendMessageTX {

    private static final Logger LOG = LoggerFactory.getLogger(SendMessageTX.class);

    public static void main(String[] args) throws JMSException {
        MQQueueConnectionFactory connectionFactory = getConnectionFactory();

        Connection connection = null;
        try {
            connection = connect(connectionFactory);
            LOG.debug("Connected to provider [{}]", connection.getMetaData().getJMSProviderName());

            // single thread scope!
            Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            Queue queue = resolveQueue("SAMPLE.STATUS_OUT", session);
            MessageProducer producer = session.createProducer(queue);

            for (int z = 0; z < 10; z++) {
                String messageBody = "Now " + System.nanoTime();
                Message mes = sendMessage(session, producer, messageBody);
                LOG.debug("Send message ID [{}]. Body [{}]", mes.getJMSMessageID(), messageBody);
            }

            LOG.debug("Ready for commit...");
            session.commit(); // fix 10 messages
            LOG.info("Commit performed.");

            // release resources
            producer.close();
            session.close();
            LOG.debug("Resource released.");
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
