package ru.codeunited.jms.service;

/**
 * Created by ikonovalov on 13.11.15.
 */
public class SchemaServiceFactory {

    public static SchemaLookupService create() {
        return new SchemaLookupServiceWebImpl();
    }

}
