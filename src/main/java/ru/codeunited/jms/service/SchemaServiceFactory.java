package ru.codeunited.jms.service;

/**
 * Created by ikonovalov on 13.11.15.
 */
public class SchemaServiceFactory {

    public static SchemaLookupService create() {
        SchemaLookupServiceWebImpl serviceWeb = new SchemaLookupServiceWebImpl();
        SchemaCacheH2Mem schemaCache = new SchemaCacheH2Mem();

        schemaCache.init();
        serviceWeb.setSchemaCache(schemaCache);

        return serviceWeb;
    }

}
