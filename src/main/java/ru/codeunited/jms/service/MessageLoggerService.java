package ru.codeunited.jms.service;

import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 14.10.15.
 */
public interface MessageLoggerService {

    void incoming(Message message);

    void incoming(TextMessage message);

    void error(Message message, Exception e);

    void handled(Message message);

    void rollback(Message message);

    void backout(String backoutQueue, String messageId, Message message);
}
