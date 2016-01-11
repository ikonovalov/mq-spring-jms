package ru.codeunited.jms.spring.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import ru.codeunited.jms.service.BusinessRequest;
import ru.codeunited.jms.service.BusinessResponse;
import ru.codeunited.jms.service.BusinessService;

/**
 * Created by ikonovalov on 11/01/16.
 */
@Service
public class MQMessageBean {

    private static final Logger LOG = LoggerFactory.getLogger(MQMessageBean.class);

    @Autowired
    private BusinessService businessService;

    @JmsListener(destination = "SAMPLE.APPLICATION_INC_2")
    public void handlerRequest(String message) {
        BusinessResponse response = businessService.processRequest(new BusinessRequest(message));
        LOG.info(response.getPayload());
    }

}
