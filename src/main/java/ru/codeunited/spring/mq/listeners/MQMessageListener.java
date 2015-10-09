package ru.codeunited.spring.mq.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.codeunited.spring.mq.service.BusinessRequest;
import ru.codeunited.spring.mq.service.BusinessResponse;
import ru.codeunited.spring.mq.service.BusinessService;

import javax.jms.*;

import static java.lang.String.format;

/**
 * Transport front-side.
 * Created by Igor on 2014.07.31.
 */
@Component
public class MQMessageListener extends AbstractMessageListener {

    @Autowired /* this is your's really business */
    private BusinessService businessService;

    @Override
    public void onMessage(Message message) {
        try {
            logMessage(message);

            if (isTextMessage(message)) {
                String payload = ((TextMessage) message).getText();

                BusinessResponse response = businessService.processRequest(new BusinessRequest(payload));

                replyIfRequired(message, response.getPayload());
            } else {

            }

        } catch (ClassCastException cce) {
            replyIfRequired(message, "We don't handle messages other then TextMessage.");
        }
        catch (Exception e) {
            getLogger().severe(e.getMessage());
            throw new RuntimeException(e);
        }
    }


}
