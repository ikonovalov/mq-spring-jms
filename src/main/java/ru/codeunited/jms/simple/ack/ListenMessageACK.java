package ru.codeunited.jms.simple.ack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.codeunited.jms.service.*;

import javax.jms.*;

import static ru.codeunited.jms.simple.JmsHelper.getConnectionFactory;
import static ru.codeunited.jms.simple.JmsHelper.resolveQueue;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 12.10.15.
 */
public class ListenMessageACK {

    private static final Logger LOG = LoggerFactory.getLogger(ListenMessageACK.class);

    private static final BusinessService service = new BusinessNumberServiceImpl();

    private static final MessageLoggerService logService = new MessageLoggerServiceImpl();

    private static final String TARGET_QUEUE = "JMS.SMPL.BUSN.REQ.ACK";

    private static final String BACKOUT_QUEUE = "JMS.SMPL.BUSN.REQ.BK";

    public static void main(String[] args) throws JMSException, InterruptedException {
        ConnectionFactory connectionFactory = getConnectionFactory();
        Connection connection = connectionFactory.createConnection("ikonovalov", "");
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        Queue queue = resolveQueue(TARGET_QUEUE, session);
        MessageConsumer consumer = session.createConsumer(queue);

        consumer.setMessageListener(
                new LogMessageListenerACK(service, logService, session)
                        .setBackoutQueue(BACKOUT_QUEUE)
        );

        connection.start();     // !DON'T FORGET!

        Thread.currentThread().join(10000L);

        // release resources
        connection.stop();
        consumer.close();
        session.close();
        connection.close();

    }

    private static class LogMessageListenerACK implements MessageListener {

        private final BusinessService service;

        private final MessageLoggerService loggerService;

        private final Session session;

        private Queue backoutQueue;

        private LogMessageListenerACK(BusinessService service, MessageLoggerService loggerService, Session session) {
            this.service = service;
            this.loggerService = loggerService;
            this.session = session;
        }

        public LogMessageListenerACK setBackoutQueue(String backoutQueueName) throws JMSException {
            this.backoutQueue = resolveQueue(backoutQueueName, session);
            return this;
        }

        @Override
        public void onMessage(Message message) {
            TextMessage textMessage = (TextMessage) message;
            logService.incoming(textMessage);
            try {

                BusinessResponse response = service.processRequest(new BusinessRequest(textMessage.getText()));

                message.acknowledge();
                logService.handled(message);

            } catch (Exception e) {
                logService.error(message, e);
                try {
                    String messageID = message.getJMSMessageID();

                    // recover or backout
                    if (message.getJMSRedelivered()) {
                        moveToBackout(message);
                        message.acknowledge();
                        logService.backout(backoutQueue.getQueueName(), messageID, message);
                    } else {
                        session.recover();
                        LOG.error("Session recovered");
                    }

                } catch (JMSException e1) {
                    LOG.error(e1.getMessage());
                }
            }
        }

        public void moveToBackout(final Message message) {
            try {
                if (backoutQueue != null) {
                    MessageProducer producer = session.createProducer(backoutQueue);
                    producer.send(message);
                    producer.close();
                }
            } catch (JMSException e) {
                LOG.error(e.getMessage());
            }
        }
    }
}
