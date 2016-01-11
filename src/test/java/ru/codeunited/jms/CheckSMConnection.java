package ru.codeunited.jms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 23.10.15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-test.xml"})
public class CheckSMConnection {

    @Qualifier("jmsQueueSingleConnectionFactory")
    @Autowired(required = true)
    private ConnectionFactory factory;

    @Autowired(required = true)
    private DefaultJmsListenerContainerFactory defaultJmsListenerContainerFactory;

    @Test
    /**
     * Just check test QM availability.
     */
    public void connect() throws JMSException {
        Connection connection = factory.createConnection();
        connection.close();
    }

}
