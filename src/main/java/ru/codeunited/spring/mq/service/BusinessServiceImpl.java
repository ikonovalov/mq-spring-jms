package ru.codeunited.spring.mq.service;

import org.springframework.stereotype.Service;

import java.util.logging.Logger;

/**
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 09.10.15.
 */
@Service
public class BusinessServiceImpl implements BusinessService {

    private Logger log = Logger.getLogger(BusinessServiceImpl.class.getName());

    @Override
    public BusinessResponse processRequest(BusinessRequest request) {
        log.info(BusinessService.class.getSimpleName() + " got message [" + request.getPayload() + "]");
        return new BusinessResponse("All right!");
    }
}
