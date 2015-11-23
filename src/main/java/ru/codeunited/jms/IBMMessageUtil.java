package ru.codeunited.jms;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Created by ikonovalov on 23/11/15.
 */
public class IBMMessageUtil {

    public static String getMessageCharSet(Message message) throws JMSException {
        return message.getStringProperty("JMS_IBM_Character_Set");
    }
}
