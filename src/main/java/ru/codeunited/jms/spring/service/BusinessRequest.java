package ru.codeunited.jms.spring.service;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 09.10.15.
 */
public final class BusinessRequest {

    private final String payload;

    public BusinessRequest(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
}
