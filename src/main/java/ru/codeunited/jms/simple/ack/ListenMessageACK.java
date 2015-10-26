package ru.codeunited.jms.simple.ack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.codeunited.jms.service.*;
import ru.codeunited.jms.simple.ExceptionHandlingStrategy;

import javax.jms.*;

import static ru.codeunited.jms.simple.JmsHelper.*;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 12.10.15.
 */
public class ListenMessageACK {

    private static final Logger LOG = LoggerFactory.getLogger(ListenMessageACK.class);

    private static final String TARGET_QUEUE = "SAMPLE.APPLICATION_INC";

    private static final String BACKOUT_QUEUE = "SAMPLE.APPLICATION_INC.BK";

    private static final long SHUTDOWN_TIMEOUT = 30000L;

    public static void main(String[] args) throws JMSException, InterruptedException {

        MessageLoggerService logService = new MessageLoggerServiceImpl();
        BusinessService service = new BusinessNumberServiceImpl();

        ConnectionFactory connectionFactory = getConnectionFactory();
        Connection connection = connect(connectionFactory);
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        Queue queue = resolveQueue(TARGET_QUEUE, session);
        MessageConsumer consumer = session.createConsumer(queue);
        consumer.setMessageListener(
                new LogMessageListenerACK(service, logService, session)
                        .setExceptionHandlingStrategy(new BackoutOnExceptionStrategy(BACKOUT_QUEUE, logService))
                //.setExceptionHandlingStrategy(new RecoverOnException())
        );

        connection.start();     // !DON'T FORGET!

        LOG.debug("Listen messages for {}ms...", SHUTDOWN_TIMEOUT);
        Thread.sleep(SHUTDOWN_TIMEOUT);

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

        private ExceptionHandlingStrategy exceptionHandlingStrategy;

        private LogMessageListenerACK(BusinessService service, MessageLoggerService loggerService, Session session) {
            this.service = service;
            this.loggerService = loggerService;
            this.session = session;
        }

        public LogMessageListenerACK setExceptionHandlingStrategy(ExceptionHandlingStrategy exceptionHandlingStrategy) {
            this.exceptionHandlingStrategy = exceptionHandlingStrategy;
            return this;
        }

        @Override
        public void onMessage(Message message) {
            TextMessage textMessage = (TextMessage) message;
            loggerService.incoming(textMessage);
            try {

                BusinessResponse response = service.processRequest(new BusinessRequest(textMessage.getText()));

                message.acknowledge();
                loggerService.handled(message);

            } catch (Exception e) {
                loggerService.error(message, e);
                exceptionHandlingStrategy.handle(session, message, e);
            }
        }
    }
}
