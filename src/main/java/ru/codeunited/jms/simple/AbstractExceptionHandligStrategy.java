package ru.codeunited.jms.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.codeunited.jms.service.MessageLoggerService;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 26.10.15.
 */
public abstract class AbstractExceptionHandligStrategy implements ExceptionHandlingStrategy {

    private final MessageLoggerService messageLoggerService;

    protected static final Logger LOG = LoggerFactory.getLogger(AbstractExceptionHandligStrategy.class);

    protected AbstractExceptionHandligStrategy(MessageLoggerService messageLoggerService) {
        this.messageLoggerService = messageLoggerService;
    }

    public MessageLoggerService getMessageLoggerService() {
        return messageLoggerService;
    }


}
