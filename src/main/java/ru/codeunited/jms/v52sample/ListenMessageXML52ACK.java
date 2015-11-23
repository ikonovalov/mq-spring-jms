package ru.codeunited.jms.v52sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.codeunited.jms.service.MessageLoggerService;
import ru.codeunited.jms.service.MessageLoggerServiceImpl;
import ru.codeunited.jms.simple.ack.ListenMessageACK;
import ru.codeunited.jms.simple.ack.strategy.BackoutOnExceptionStrategy;
import ru.codeunited.jms.v52sample.impl.MessageFileStore;
import ru.codeunited.jms.v52sample.impl.MessageV52ListenerACK;
import ru.codeunited.jms.v52sample.impl.MessageV52Validator;

import javax.jms.*;
import java.util.Scanner;

import static ru.codeunited.jms.simple.JmsHelper.*;

/**
 * Created by ikonovalov on 20/11/15.
 */
public class ListenMessageXML52ACK {

    private static final Logger LOG = LoggerFactory.getLogger(ListenMessageACK.class);

    private static final String TARGET_QUEUE = "SAMPLE.APPLICATION_INC";

    private static final String BACKOUT_QUEUE = "SAMPLE.APPLICATION_INC.BK";

    static final String STATUS_QUEUE = "SAMPLE.STATUS_OUT";

    private static final long SHUTDOWN_TIMEOUT = 10000L;


    public static void main(String[] args) throws JMSException, InterruptedException {

        // connect to JMS provider
        ConnectionFactory connectionFactory = getConnectionFactory();
        Connection connection = connect(connectionFactory);

        // create consumer session and consumer itself
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        Queue queue = resolveQueue(TARGET_QUEUE, session);
        MessageConsumer consumer = session.createConsumer(queue);

        MessageLoggerService loggerService = new MessageLoggerServiceImpl();

        // create and setup consumer (message listener)
        MessageV52ListenerACK messageListener = new MessageV52ListenerACK(loggerService, session);
        messageListener.setExceptionHandlingStrategy(new BackoutOnExceptionStrategy(BACKOUT_QUEUE, loggerService));
        messageListener.setValidator(new MessageV52Validator());
        messageListener.setMessageStorage(new MessageFileStore());
        messageListener.setStatusQueueName(STATUS_QUEUE);
        messageListener.init();

        consumer.setMessageListener(messageListener);
        connection.start();

        // put message for test purpose
        putTestMessage(connection);

        // listening and shutdown
        LOG.debug("Listen messages for {}ms...", SHUTDOWN_TIMEOUT);
        Thread.sleep(SHUTDOWN_TIMEOUT);

        // release resources
        connection.stop();
        consumer.close();
        session.close();
        connection.close();
    }

    public static void putTestMessage(Connection connection) throws JMSException {
        // The JMS specification does not permit the use of a session for synchronous methods when asynchronous message delivery is running
        Session producerSession = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        Queue putQueue = resolveQueue(TARGET_QUEUE, producerSession);
        MessageProducer producer = producerSession.createProducer(putQueue);
        TextMessage message = producerSession.createTextMessage();
        String messageBody = new Scanner(ListenMessageXML52ACK.class.getResourceAsStream("/message_bad.xml"), "UTF-8").useDelimiter("\\A").next();
        message.setText(messageBody);
        producer.send(message);
        LOG.debug("Message sent [{}]", message.getJMSMessageID());
        // release put's resources
        producer.close();
        producerSession.close();
    }


}
