package ru.codeunited.jms.service;

import javax.jms.Message;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 14.10.15.
 */
public interface MessageLoggerService {

    void incoming(Message message);

    void error(Message message, Exception e);

    void handled(Message message);
}
