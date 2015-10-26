package ru.codeunited.jms.simple;

import javax.jms.Message;
import javax.jms.Session;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 26.10.15.
 */
public interface ExceptionHandlingStrategy {

    void handle(Session session, Message message, Exception e);

}
