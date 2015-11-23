package ru.codeunited.jms.v52sample.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.codeunited.jms.v52sample.MessageStorage;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static ru.codeunited.jms.IBMMessageUtil.getMessageCharSet;

/**
 * Created by ikonovalov on 23/11/15.
 */
public class MessageFileStore implements MessageStorage<File> {

    private static final Logger LOG = LoggerFactory.getLogger(MessageFileStore.class);

    @Override
    public File store(Message message) throws IOException, JMSException {
        if (message instanceof TextMessage) {
            return storeMessage((TextMessage) message);
        } else {
            return null;
        }
    }

    private File storeMessage(TextMessage message) throws JMSException, IOException {
        File storeFile = new File(String.format("/tmp/jms-%s.msg", message.getJMSMessageID().replaceAll("ID:", "")));
        try (FileOutputStream fis = new FileOutputStream(storeFile)) {
            fis.write(message.getText().getBytes(getMessageCharSet(message))); // don't forget right encoding!
        }
        LOG.info("Message stored in a file {}", storeFile.getAbsolutePath());
        return storeFile;
    }
}
