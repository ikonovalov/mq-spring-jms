package ru.codeunited.jms;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import ru.codeunited.jms.service.SchemaLookupService;
import ru.codeunited.jms.service.SchemaServiceFactory;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;

/**
 * Created by ikonovalov on 13.11.15.
 */
public class ValidateOriginalMessagesTest {

    private Logger logger = LoggerFactory.getLogger(ValidateOriginalMessagesTest.class);

    private SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    private Source schemaV52 = new StreamSource(ValidateOriginalMessagesTest.class.getResourceAsStream("/v5_2.xsd"));


    @Test(expected = SAXParseException.class)
    public void validate() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        SchemaLookupService schemaLookupService = SchemaServiceFactory.create();

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document document = builder.parse(ValidateOriginalMessagesTest.class.getResourceAsStream("/message_bad.xml"));


        // extract serviceCode and perform schema augmentation
        XPath xpath = XPathFactory.newInstance().newXPath();
        String serviceCode = (String) xpath.evaluate("//*[local-name()='ServiceTypeCode']/text()", document, XPathConstants.STRING);
        Schema schema;
        if (serviceCode != null) {
            schema = factory.newSchema(new Source[]{schemaV52, schemaLookupService.lookupForService(serviceCode)});
        } else {
            schema = factory.newSchema(schemaV52);
        }
        logger.debug("Schema uploaded.");


        // perform validation
        Validator validator = schema.newValidator(); // not thread safe and not reenter
        logger.debug("Validator ready.");

        try {
            validator.validate(new DOMSource(document));
        } catch (SAXException e) {
            logger.error("Validation failed. {}", e.getMessage());
            throw e;
        }
    }

    @Test
    public void validateWithDetachedCustomAttributes() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document document = builder.parse(ValidateOriginalMessagesTest.class.getResourceAsStream("/message_bad.xml"));

        // detach CustomAttributes
        XPath xpathCustomAttributes = XPathFactory.newInstance().newXPath();
        NodeList customAttributes = (NodeList) xpathCustomAttributes.evaluate("//*[local-name()='CustomAttributes']", document, XPathConstants.NODESET);
        for (int n = 0; n < customAttributes.getLength(); n++) {
            Element customAttrNode = (Element) customAttributes.item(n);
            customAttrNode.getParentNode().removeChild(customAttrNode);
            logger.debug("Remove node {} ", customAttrNode);
        }

        // prepare schema
        Schema schema = factory.newSchema(schemaV52);

        logger.debug("Schema uploaded.");


        // perform validation
        Validator validator = schema.newValidator(); // not thread safe and not reenter
        logger.debug("Validator ready.");

        try {
            validator.validate(new DOMSource(document));
        } catch (SAXException e) {
            logger.error("Validation failed. {}", e.getMessage());
            throw e;
        }
    }
}
