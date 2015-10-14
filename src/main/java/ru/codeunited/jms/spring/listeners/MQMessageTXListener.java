package ru.codeunited.jms.spring.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.codeunited.jms.service.BusinessRequest;
import ru.codeunited.jms.service.BusinessResponse;
import ru.codeunited.jms.service.BusinessService;

import javax.jms.*;

/**
 * Transport front-side.
 * Created by Igor on 2014.07.31.
 */
@Component
public class MQMessageTXListener extends AbstractMessageListener {

    @Autowired /* this is your's really business */
    private BusinessService businessService;

    @Override
    public void onMessage(Message message) {
        try {
            logMessage(message);
            String payload = ((TextMessage) message).getText();
            BusinessResponse response = businessService.processRequest(new BusinessRequest(payload));
            replyIfRequired(message, response.getPayload());

        } catch (ClassCastException cce) {
            replyIfRequired(message, "We don't handle messages other then TextMessage.");
        } catch (Exception e) {
            getLogger().error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}