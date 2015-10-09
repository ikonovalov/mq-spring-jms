package ru.codeunited.spring.mq.sender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.destination.DestinationResolver;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * This is a just builder pattern, so all impl should be prototype. @see TextMessageCreator
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 09.10.15.
 */
public abstract class GenericMessageCreator<T, JmsTyped extends Message> implements MessageCreator {

    private T messageBody;

    private String replyToQueue;

    private JmsTyped messageHolder;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private DestinationResolver destinationResolver;

    protected GenericMessageCreator() {
        super();
    }

    public GenericMessageCreator setMessageBody(T messageBody) {
        this.messageBody = messageBody;
        return this;
    }

    protected T getMessageBody() {
        return messageBody;
    }

    public GenericMessageCreator setReplyToQueue(String replyToQueue) {
        this.replyToQueue = replyToQueue;
        return this;
    }

    /**
     * You should create appropriate typed message and attache body.
     * Don't extends visibility. It's inner use method.
     * @param session
     * @param messageBody
     * @return
     */
    protected abstract JmsTyped createTypedMessage(Session session, T messageBody);

    @Override
    public Message createMessage(Session session) throws JMSException {
        JmsTyped tm = createTypedMessage(session, getMessageBody());
        if (needReply()) {
            tm.setJMSReplyTo(destinationResolver.resolveDestinationName(session, this.replyToQueue, false));
        }
        messageHolder = tm;
        return tm;
    }

    public JmsTyped getMessage() {
        return messageHolder;
    }

    protected boolean needReply() {
        return replyToQueue != null;
    }
}
