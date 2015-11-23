package ru.codeunited.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.codeunited.jms.service.MessageLoggerService;
import ru.codeunited.jms.service.MessageLoggerServiceImpl;
import ru.codeunited.jms.simple.ExceptionHandlingStrategy;
import ru.codeunited.jms.simple.ack.ListenMessageACK;
import ru.codeunited.jms.simple.ack.strategy.BackoutOnExceptionStrategy;

import javax.jms.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.Scanner;

import static ru.codeunited.jms.simple.JmsHelper.*;

/**
 * Created by ikonovalov on 20/11/15.
 */
public class ListemMessageXML52ACK {

    private static final Logger LOG = LoggerFactory.getLogger(ListenMessageACK.class);

    private static final String TARGET_QUEUE = "SAMPLE.APPLICATION_INC";

    private static final String BACKOUT_QUEUE = "SAMPLE.APPLICATION_INC.BK";

    private static final String STATUS_QUEUE = "SAMPLE.STATUS_OUT";

    private static final long SHUTDOWN_TIMEOUT = 10000L;

    private static SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    private static Source schemaV52 = new StreamSource(ValidateOriginalMessagesTest.class.getResourceAsStream("/v5_2.xsd"));

    private static final String NS_V52 = "http://asguf.mos.ru/rkis_gu/coordinate/v5_2/";

    public static void main(String[] args) throws JMSException, InterruptedException {

        // connect to JMS provider
        ConnectionFactory connectionFactory = getConnectionFactory();
        Connection connection = connect(connectionFactory);

        // create consumer session and consumer itself
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        Queue queue = resolveQueue(TARGET_QUEUE, session);
        MessageConsumer consumer = session.createConsumer(queue);

        MessageLoggerService loggerService = new MessageLoggerServiceImpl();

        // create and setup consumet (message listener)
        MessageListenerImpl messageListener = new MessageListenerImpl(loggerService, session);
        messageListener.init();
        messageListener.setExceptionHandlingStrategy(new BackoutOnExceptionStrategy(BACKOUT_QUEUE, loggerService));
        consumer.setMessageListener(messageListener);
        connection.start();

        // put message for test purpose
        putTestMessage(connection);

        // listening and shutdown
        LOG.debug("Listen messages for {}ms...", SHUTDOWN_TIMEOUT);
        Thread.sleep(SHUTDOWN_TIMEOUT);

        // release resources
        connection.stop();
        consumer.close();
        session.close();
        connection.close();
    }

    public static void putTestMessage(Connection connection) throws JMSException {
        // The JMS specification does not permit the use of a session for synchronous methods when asynchronous message delivery is running
        Session producerSession = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        Queue putQueue = resolveQueue(TARGET_QUEUE, producerSession);
        MessageProducer producer = producerSession.createProducer(putQueue);
        TextMessage message = producerSession.createTextMessage();
        String messageBody = new Scanner(ListemMessageXML52ACK.class.getResourceAsStream("/message_bad.xml"), "UTF-8").useDelimiter("\\A").next();
        message.setText(messageBody);
        producer.send(message);
        LOG.debug("Message sent [{}]", message.getJMSMessageID());
        // release put's resources
        producer.close();
        producerSession.close();
    }

    public static class MessageListenerImpl implements MessageListener {

        private final MessageLoggerService loggerService;

        private ExceptionHandlingStrategy exceptionHandlingStrategy;

        private Session session;

        private MessageProducer statusProducer;

        public MessageListenerImpl(MessageLoggerService loggerService, Session session) {
            this.loggerService = loggerService;
            this.session = session;
        }

        public void init() throws JMSException {
            this.statusProducer = session.createProducer(resolveQueue(STATUS_QUEUE, session));
        }

        public MessageListenerImpl setExceptionHandlingStrategy(ExceptionHandlingStrategy exceptionHandlingStrategy) {
            this.exceptionHandlingStrategy = exceptionHandlingStrategy;
            return this;
        }

        @Override
        public void onMessage(Message message) {
            TextMessage textMessage = (TextMessage) message;
            // log incoming message
            loggerService.incoming(textMessage);
            try {
                String textBody = textMessage.getText();
                // validate message
                Document validDocument = validateWithDetachedCustomAttributes(new ByteArrayInputStream(textBody.getBytes(getMessageCharSet(message))));

                // store message
                storeMessage(textMessage);

                // send status message
                // 1. extract ServiceTypeCode
                XPath xpath = XPathFactory.newInstance().newXPath();
                String serviceNumber = (String) xpath.evaluate(
                        "//*[local-name()='ServiceNumber']/text()" /*sure, you can do it much better*/,
                        validDocument,
                        XPathConstants.STRING
                );
                sendStatus(serviceNumber);

                textMessage.acknowledge();
            } catch (Exception e) {
                loggerService.error(message, e);
                exceptionHandlingStrategy.handle(session, message, e);
            }
        }

        public String getMessageCharSet(Message message) throws JMSException {
            return message.getStringProperty("JMS_IBM_Character_Set");
        }

        /**
         * <out:StatusMessage xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         * xmlns="http://asguf.mos.ru/rkis_gu/coordinate/v5_2/"
         * xmlns:out="http://asguf.mos.ru/rkis_gu/coordinate/v5_2/">
         * <out:ResponseDate>2015-10-16T12:18:36.177Z</out:ResponseDate>
         * <out:PlanDate xsi:nil="true"/>
         * <out:StatusCode>1040</out:StatusCode>
         * <out:StatusDate>2015-10-16T12:18:36.177Z</out:StatusDate>
         * <out:ServiceNumber>0752-9000002-043301-6003707/15</out:ServiceNumber>
         * </out:StatusMessage>
         *
         * @param serviceCode
         * @throws ParserConfigurationException
         */
        private void sendStatus(String serviceCode) throws ParserConfigurationException, TransformerException, JMSException {
            if (statusProducer == null) {
                LOG.warn("Status producer not initialized. Status skipped.");
                return;
            }
            String statusMessageText = "<out:StatusMessage xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "                   xmlns=\"http://asguf.mos.ru/rkis_gu/coordinate/v5_2/\"\n" +
                    "                   xmlns:out=\"http://asguf.mos.ru/rkis_gu/coordinate/v5_2/\">\n" +
                    "    <out:ResponseDate>2015-10-16T12:18:36.177Z</out:ResponseDate>\n" +
                    "    <out:PlanDate xsi:nil=\"true\"/>\n" +
                    "    <out:StatusCode>1040</out:StatusCode>\n" +
                    "    <out:StatusDate>2015-10-16T12:18:36.177Z</out:StatusDate>\n" +
                    "    <out:ServiceNumber>%1$s</out:ServiceNumber>\n" +
                    "</out:StatusMessage>".intern();
            String substitutedMessageText = String.format(statusMessageText, serviceCode);
            TextMessage statusMessage = session.createTextMessage();
            statusMessage.setText(substitutedMessageText);
            statusProducer.send(statusMessage);
            LOG.info("Status message {} was sent for {}", statusMessage.getJMSMessageID(), serviceCode);
        }


        private void storeMessage(TextMessage message) throws JMSException, IOException {
            String storeFileName = String.format("/tmp/jms-%s.msg", message.getJMSMessageID().replaceAll("ID:", ""));
            try (FileOutputStream fis = new FileOutputStream(storeFileName)) {
                fis.write(message.getText().getBytes(getMessageCharSet(message))); // don't forget right encoding!
            }
            LOG.info("Message stored in a file {}", storeFileName);
        }
    }

    public static Document validateWithDetachedCustomAttributes(InputStream stream) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document document = builder.parse(stream);

        // detach CustomAttributes
        XPath xpathCustomAttributes = XPathFactory.newInstance().newXPath();
        NodeList customAttributes = (NodeList) xpathCustomAttributes.evaluate("//*[local-name()='CustomAttributes']", document, XPathConstants.NODESET);
        for (int n = 0; n < customAttributes.getLength(); n++) {
            Element customAttrNode = (Element) customAttributes.item(n);
            customAttrNode.getParentNode().removeChild(customAttrNode);
        }
        LOG.debug("Detach {} CustomAttributes", customAttributes.getLength());
        // prepare schema
        Schema schema = factory.newSchema(schemaV52);

        LOG.debug("Schema uploaded.");


        // perform validation
        Validator validator = schema.newValidator(); // not thread safe and not reenter
        LOG.debug("Validator ready.");

        try {
            validator.validate(new DOMSource(document));
            LOG.info("Schema validated.");
        } catch (SAXException e) {
            LOG.error("Schema validation failed. {}", e.getMessage());
            throw e;
        }
        return document;
    }
}
