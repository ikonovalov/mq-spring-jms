package ru.codeunited.jms.spring.service;

/**
 * This is a business service. Like many other.
 * codeunited.ru
 * konovalov84@gmail.com
 * Created by ikonovalov on 09.10.15.
 */
public interface BusinessService {

    BusinessResponse processRequest(BusinessRequest request);

}
