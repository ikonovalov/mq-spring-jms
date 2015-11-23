package ru.codeunited.jms.v52sample;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

/**
 * Created by ikonovalov on 23/11/15.
 */
public interface MessageValidator {

    public Document validate(Message message) throws ParserConfigurationException, XPathExpressionException, SAXException, IOException, JMSException, Exception;

}
