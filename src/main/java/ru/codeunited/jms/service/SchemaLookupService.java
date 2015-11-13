package ru.codeunited.jms.service;

import javax.xml.transform.Source;
import java.io.IOException;

/**
 * Created by ikonovalov on 13.11.15.
 */
public interface SchemaLookupService {
    Source lookupForService(String serviceCode) throws IOException;
}
