package ru.codeunited.jms.simple.tx;

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
public class ListenMessageTX {

    private static final Logger LOG = LoggerFactory.getLogger(ListenMessageTX.class);

    private static final BusinessService service = new BusinessNumberServiceImpl();

    private static final MessageLoggerService logService = new MessageLoggerServiceImpl();

    private static final String TARGET_QUEUE = "JMS.SMPL.BUSN.REQ.TX";

    public static void main(String[] args) throws JMSException, InterruptedException {
        ConnectionFactory connectionFactory = getConnectionFactory();
        Connection connection = connectionFactory.createConnection("ikonovalov", "");
        final Session session = connection.createSession(true, Session.CLIENT_ACKNOWLEDGE);

        Queue queue = resolveQueue(TARGET_QUEUE, session);

        MessageConsumer consumer = session.createConsumer(queue);
        consumer.setMessageListener(new LogMessageListenerTX(session, service, logService));

        connection.start();     // !DON'T FORGET!

        Thread.currentThread().join(10000L); // Consume only 10 second

        // release resources
        connection.stop();
        consumer.close();
        session.close();
        connection.close();

    }

    private static class LogMessageListenerTX implements MessageListener {

        private final Session session;

        private final BusinessService service;

        private final MessageLoggerService loggerService;

        public LogMessageListenerTX(Session session, BusinessService service, MessageLoggerService loggerService) {
            this.session = session;
            this.service = service;
            this.loggerService = loggerService;
        }

        @Override
        public void onMessage(Message message) {
            TextMessage textMessage = (TextMessage) message;
            loggerService.incoming(message);
            try {

                BusinessResponse response = service.processRequest(new BusinessRequest(textMessage.getText()));

                session.commit();
                loggerService.handled(message);

            } catch (Exception e) {
                loggerService.error(message, e);

                try {
                    session.rollback();
                    loggerService.rollback(message);
                } catch (JMSException e1) {
                    LOG.error(e1.getMessage());
                }
            }
        }
    }
}
