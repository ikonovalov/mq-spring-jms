package ru.codeunited.jms.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

import javax.jms.ConnectionFactory;

/**
 * Created by ikonovalov on 11/01/16.
 */
@Configuration
@EnableJms
public class JMSContainerConfiguration {

    @Autowired
    ConnectionFactory jmsQueueSingleConnectionFactory;

    @Autowired
    DynamicDestinationResolver destinationResolver;

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(jmsQueueSingleConnectionFactory);
        factory.setDestinationResolver(destinationResolver);
        factory.setSessionTransacted(true);
        factory.setConcurrency("1-5");
        return factory;
    }
}
