package ru.codeunited.jms.spring.sender;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 09.10.15.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TextMessageCreator extends GenericMessageCreator<String, TextMessage> {

    @Override
    protected TextMessage createTypedMessage(Session session, String messageBody) {
        try {
            TextMessage message = session.createTextMessage();
            message.setText(messageBody);
            return message;
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
