package ru.codeunited.jms.v52sample.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import ru.codeunited.jms.service.MessageLoggerService;
import ru.codeunited.jms.simple.ExceptionHandlingStrategy;
import ru.codeunited.jms.v52sample.ListenMessageXML52ACK;
import ru.codeunited.jms.v52sample.MessageStorage;
import ru.codeunited.jms.v52sample.MessageValidator;

import javax.jms.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import static ru.codeunited.jms.simple.JmsHelper.resolveQueue;

/**
 * Created by ikonovalov on 23/11/15.
 */
public class MessageV52ListenerACK implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(MessageV52ListenerACK.class);
    private final MessageLoggerService loggerService;

    private ExceptionHandlingStrategy exceptionHandlingStrategy;

    private Session session;

    private MessageProducer statusProducer;

    private MessageValidator validator;

    private MessageStorage messageStorage;

    private String statusQueueName;

    public MessageV52ListenerACK(MessageLoggerService loggerService, Session session) {
        this.loggerService = loggerService;
        this.session = session;
    }

    public MessageValidator getValidator() {
        return validator;
    }

    public void setValidator(MessageValidator validator) {
        this.validator = validator;
    }

    public MessageStorage getMessageStorage() {
        return messageStorage;
    }

    public void setMessageStorage(MessageStorage messageStorage) {
        this.messageStorage = messageStorage;
    }

    public String getStatusQueueName() {
        return statusQueueName;
    }

    public void setStatusQueueName(String statusQueueName) {
        this.statusQueueName = statusQueueName;
    }

    /**
     * Initialize internal features.
     *
     * @throws JMSException
     */
    public void init() throws JMSException {
        this.statusProducer = session.createProducer(resolveQueue(getStatusQueueName(), session));
    }

    public MessageV52ListenerACK setExceptionHandlingStrategy(ExceptionHandlingStrategy exceptionHandlingStrategy) {
        this.exceptionHandlingStrategy = exceptionHandlingStrategy;
        return this;
    }

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        // log incoming message
        loggerService.incoming(textMessage);
        try {
            // validate message
            Document validDocument = getValidator().validate(textMessage);

            // messageStorage message
            getMessageStorage().store(textMessage);

            // send status message
            XPath xpath = XPathFactory.newInstance().newXPath();
            String serviceNumber = (String) xpath.evaluate(
                    "//*[local-name()='ServiceNumber']/text()" /*sure, you can do it much better*/,
                    validDocument,
                    XPathConstants.STRING
            );
            sendStatus(serviceNumber, "1040");

            textMessage.acknowledge();
        } catch (Exception e) {
            loggerService.error(message, e);
            exceptionHandlingStrategy.handle(session, message, e);
        }
    }

    protected void sendStatus(String serviceCode, String statusCode) throws ParserConfigurationException, TransformerException, JMSException {
        if (statusProducer == null) {
            LOG.warn("Status producer not initialized. Status skipped.");
            return;
        }
        String statusMessageText = "<out:StatusMessage xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "                   xmlns=\"http://asguf.mos.ru/rkis_gu/coordinate/v5_2/\"\n" +
                "                   xmlns:out=\"http://asguf.mos.ru/rkis_gu/coordinate/v5_2/\">\n" +
                "    <out:ResponseDate>2015-10-16T12:18:36.177Z</out:ResponseDate>\n" +
                "    <out:PlanDate xsi:nil=\"true\"/>\n" +
                "    <out:StatusCode>%2$s</out:StatusCode>\n" +
                "    <out:StatusDate>2015-10-16T12:18:36.177Z</out:StatusDate>\n" +
                "    <out:ServiceNumber>%1$s</out:ServiceNumber>\n" +
                "</out:StatusMessage>".intern();
        String substitutedMessageText = String.format(statusMessageText, serviceCode, statusCode);
        TextMessage statusMessage = session.createTextMessage();
        statusMessage.setText(substitutedMessageText);
        statusProducer.send(statusMessage);
        LOG.info("Status message {} was sent for {}", statusMessage.getJMSMessageID(), serviceCode);
    }

}
