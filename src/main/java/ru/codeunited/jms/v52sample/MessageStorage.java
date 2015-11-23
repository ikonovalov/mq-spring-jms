package ru.codeunited.jms.v52sample;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;

/**
 * Created by ikonovalov on 23/11/15.
 */
public interface MessageStorage<T> {

    public T store(Message message) throws IOException, JMSException;

}
