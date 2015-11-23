package ru.codeunited.jms.v52sample.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.codeunited.jms.v52sample.MessageValidator;

import javax.jms.Message;
import javax.jms.TextMessage;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;

import static ru.codeunited.jms.IBMMessageUtil.getMessageCharSet;

/**
 * Created by ikonovalov on 23/11/15.
 */
public class MessageV52Validator implements MessageValidator {

    private static final Logger LOG = LoggerFactory.getLogger(MessageV52Validator.class);

    private static SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    private static Source schemaV52 = new StreamSource(MessageV52Validator.class.getResourceAsStream("/v5_2.xsd"));


    @Override
    public Document validate(Message message) throws Exception {
        TextMessage textMessage = (TextMessage) message;
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(textMessage.getText().getBytes(getMessageCharSet(message))));

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
