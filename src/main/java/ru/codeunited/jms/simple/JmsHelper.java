package ru.codeunited.jms.simple;

import com.ibm.mq.jms.MQQueueConnectionFactory;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 12.10.15.
 */
public class JmsHelper {

    public static MQQueueConnectionFactory getConnectionFactory() throws JMSException {
        MQQueueConnectionFactory connectionFactory = new MQQueueConnectionFactory();
        connectionFactory.setHostName("ubuntu540");
        connectionFactory.setPort(1414);
        connectionFactory.setQueueManager("DEFQM");
        connectionFactory.setTransportType(1); // => MQConstants.TRANSPORT_MQSERIES_CLIENT
        connectionFactory.setCCSID(1208);
        connectionFactory.setChannel("JVM.DEF.SVRCONN");
        return connectionFactory;
    }

    public static Queue resolveQueue(String name, Session session) throws JMSException {
        return session.createQueue(name);
    }
}
