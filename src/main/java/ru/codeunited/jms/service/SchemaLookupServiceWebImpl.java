package ru.codeunited.jms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Created by ikonovalov on 13.11.15.
 */
public class SchemaLookupServiceWebImpl implements SchemaLookupService {

    private Logger logger = LoggerFactory.getLogger(SchemaLookupServiceWebImpl.class);

    private final String baseUrl;

    private SchemaCache schemaCache = new SchemaCache() { // default implementation
        @Override
        public Source lookupByServiceTypeCode(String code) {
            return null;
        }
    };

    SchemaLookupServiceWebImpl() {
        this("http://212.45.30.101:81/Information/GetCustomAttributesSchemaHandler.ashx");
    }

    public SchemaLookupServiceWebImpl(String url) {
        this.baseUrl = url;
    }

    protected SchemaCache getSchemaCache() {
        return schemaCache;
    }

    protected void setSchemaCache(SchemaCache schemaCache) {
        this.schemaCache = schemaCache;
    }

    @Override
    public Source lookupForService(String serviceCode) throws IOException {
        if (serviceCode == null)
            return null;
        Source cached = schemaCache.lookupByServiceTypeCode(serviceCode);
        if (cached == null) {
            URL url = new URL(baseUrl + "?servicecode=" + serviceCode);
            //  TODO: 13.11.15 store in a cache
            return new StreamSource(new BufferedInputStream(url.openStream()));
        } else {
            return cached;
        }
    }

}
