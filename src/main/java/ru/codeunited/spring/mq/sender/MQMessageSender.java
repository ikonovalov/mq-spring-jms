package ru.codeunited.spring.mq.sender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Message;

/**
 * Singleton, thread-safe, fine.
 * Created by Igor on 2014.07.31.
 */
@Component
public class MQMessageSender implements MessageSender {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ApplicationContext context;

    /**
     * Send message to destination without reply.
     * @param message
     * @param destinationQueue
     */
    @Override
    public Message send(String message, String destinationQueue) {
        return send(message, destinationQueue, null);
    }

    /**
     * Send message to destination with reply.
     * @param message
     * @param destinationQueue
     * @param replyToQueue
     */
    @Override
    public Message send(String message, String destinationQueue, String replyToQueue) {
        GenericMessageCreator creator = context.getBean(TextMessageCreator.class)
                .setMessageBody(message)
                .setReplyToQueue(replyToQueue);
        jmsTemplate.send(destinationQueue, creator);
        return creator.getMessage();
    }
}
