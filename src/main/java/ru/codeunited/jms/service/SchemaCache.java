package ru.codeunited.jms.service;

import javax.xml.transform.Source;

/**
 * Created by ikonovalov on 13.11.15.
 */
public interface SchemaCache {

    public Source lookupByServiceTypeCode(String code);

}
