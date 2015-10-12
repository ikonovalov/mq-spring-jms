package ru.codeunited.jms.spring.sender;

import javax.jms.Message;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 09.10.15.
 */
public interface MessageSender {

    Message send(String message, String destinationQueue);

    Message send(String message, String destinationQueue, String replyToQueue);

}
