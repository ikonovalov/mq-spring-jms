package ru.codeunited.jms.simple.ack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.codeunited.jms.service.*;

import javax.jms.*;

import static ru.codeunited.jms.simple.JmsHelper.*;


/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 12.10.15.
 */
public class ReceiveMessageACK {

    private static final Logger LOG = LoggerFactory.getLogger(ReceiveMessageACK.class);

    private static final BusinessService service = new BusinessNumberServiceImpl();

    private static final MessageLoggerService logService = new MessageLoggerServiceImpl();

    private static final String TARGET_QUEUE = "SAMPLE.APPLICATION_INC";

    private static final String BACKOUT_QUEUE = "SAMPLE.APPLICATION_INC.BK";

    private static final long TIMEOUT = 1000L;

    public static void main(String[] args) throws JMSException {
        ConnectionFactory connectionFactory = getConnectionFactory();
        Connection connection = connect(connectionFactory);
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        Queue queue = resolveQueue(TARGET_QUEUE, session);
        MessageConsumer consumer = session.createConsumer(queue);

        connection.start();             // !DON'T FORGET!
        TextMessage message = (TextMessage) consumer.receive(TIMEOUT); // or receive(), or receiveNoWait
        while (message != null) {       // null - queue is empty.
            logService.incoming(message);
            try {
                /* it accepts numbers only. So, if you put alphabet it should throw exception */
                BusinessResponse response = service.processRequest(new BusinessRequest(message.getText()));

                message.acknowledge();  // we use CLIENT_ACKNOWLEDGE
                logService.handled(message);
            } catch (Exception e) {
                logService.error(message, e);

                // backout or redelivery
                if (message.getJMSRedelivered()) { // or use JMSXDeliveryCount to makes decision
                    String originalMessageId = message.getJMSMessageID();
                    moveToBackout(BACKOUT_QUEUE, session, message, e);
                    logService.backout(BACKOUT_QUEUE, originalMessageId, message);
                    message.acknowledge();
                } else {
                    session.recover();
                    LOG.warn("Session recovered");
                }
            }
            message = (TextMessage) consumer.receive(TIMEOUT);
        }

        LOG.info("Queue is empty");

        // release resources
        connection.stop();
        consumer.close();
        session.close();
        connection.close();

    }

    public static void moveToBackout(String backoutQueue, Session session, Message message, Exception exception) {
        try {
            MessageProducer producer = session.createProducer(session.createQueue(backoutQueue));
            Message messageCopy = copyMessage(session, message);
            messageCopy.setStringProperty("Error", exception.getMessage());
            producer.send(messageCopy);
            producer.close();

        } catch (JMSException e) {
            LOG.error(e.getMessage());
        }
    }

}
