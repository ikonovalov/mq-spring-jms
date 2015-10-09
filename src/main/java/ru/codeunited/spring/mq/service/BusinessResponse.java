package ru.codeunited.spring.mq.service;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 09.10.15.
 */
public final class BusinessResponse {

    private final String payload;

    public BusinessResponse(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
}
