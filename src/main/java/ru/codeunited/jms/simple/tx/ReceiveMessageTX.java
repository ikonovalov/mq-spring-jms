package ru.codeunited.jms.simple.tx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.codeunited.jms.service.*;

import javax.jms.*;

import static ru.codeunited.jms.simple.JmsHelper.connect;
import static ru.codeunited.jms.simple.JmsHelper.getConnectionFactory;
import static ru.codeunited.jms.simple.JmsHelper.resolveQueue;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 12.10.15.
 */
public class ReceiveMessageTX {

    private static final Logger LOG = LoggerFactory.getLogger(ReceiveMessageTX.class);

    private static final BusinessService service = new BusinessNumberServiceImpl();

    private static final MessageLoggerService logService = new MessageLoggerServiceImpl();

    private static final String TARGET_QUEUE = "SAMPLE.APPLICATION_INC";

    private static final long TIMEOUT = 1000L;

    public static void main(String[] args) throws JMSException {
        ConnectionFactory connectionFactory = getConnectionFactory();
        Connection connection = connect(connectionFactory);

        // WORK UNIT START
        Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
        Queue queue = resolveQueue(TARGET_QUEUE, session);
        MessageConsumer consumer = session.createConsumer(queue);

        connection.start();

        TextMessage message = (TextMessage) consumer.receive(TIMEOUT);
        while (message != null) {
            logService.incoming(message);

            try {
                BusinessResponse response = service.processRequest(new BusinessRequest(message.getText()));

                session.commit(); // We use transacted session
                logService.handled(message);
            } catch (Exception e) {
                logService.error(message, e);
                session.rollback();
                logService.rollback(message);
            }
            message = (TextMessage) consumer.receive(TIMEOUT);
        }
        // WORK UNIT END

        // release resources
        connection.stop();
        consumer.close();
        session.close();
        connection.close();

    }

}
